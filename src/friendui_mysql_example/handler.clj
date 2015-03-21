(ns friendui-mysql-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [net.cgrand.enlive-html :as html :refer [deftemplate content substitute clone-for]]
            [dossier.utils :as utils]
            [cemerick.friend :as friend]
            [cemerick.friend [workflows :as workflows]
                             [credentials :as creds]]
            [de.sveri.friendui.routes.user :refer [friend-routes]]
            [de.sveri.friendui.globals :as f-global]
            [sventechie.friendui-mysql.db :refer [get-usermap-by-username]]
            [sventechie.friendui-mysql.storage :refer :all]
            [sventechie.friendui-mysql.globals :refer [friendui-config]])
  (:import [sventechie.friendui-mysql.storage.FrienduiStorage]))

(def FrienduiStorageImpl (->FrienduiStorage (:database friendui-config)))

(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn get-usermap-by-username)
   :workflows                 [(workflows/interactive-form)]
   :login-uri                 "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri       "/"})

(def template-path "templates/de/sveri/friendui/user/")
;(alter-var-root #'f-global/template-path template-path)
(html/deftemplate base (str template-path "base.html")
                  [{:keys [title content]}]
                  [:#title] (utils/maybe-content title)         ; :base-template-title-key key in the config
                  [:#content] (utils/maybe-substitute content)) ; :base-template-content-key key in the config

(alter-var-root #'f-global/base-template (fn [_] (partial base)))

(defn authenticate-routes
  "Add Friend handler to routes"
  [routes-set]
  (handler/site
    (friend/authenticate routes-set friend-settings)))

(def friendui-callbacks {:signup-succ-func (fn[] "success")
                         :activate-account-succ-func (fn[user-map] "activated")})

(defroutes app-routes
  (GET "/" [] "Hello World")
  (friend-routes FrienduiStorageImpl)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
     (wrap-defaults site-defaults)
     (authenticate-routes)))
