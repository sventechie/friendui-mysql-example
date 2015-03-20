(ns friendui-mysql-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [cemerick.friend :as friend]
            [cemerick.friend [workflows :as workflows]
                             [credentials :as creds]]
            [de.sveri.friendui.routes.user :refer [friend-routes]]
            [sventechie.friendui-mysql.db :refer :all]
            [sventechie.friendui-mysql.storage :refer :all]
            [sventechie.friendui-mysql.globals :refer :all])

  (:import [sventechie.friendui-mysql.storage.FrienduiStorage]))

(def FrienduiStorageImpl (->FrienduiStorage database))

(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn get-usermap-by-username)
   :workflows                 [(workflows/interactive-form)]
   :login-uri                 "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri       "/"})

(defn authenticate-routes
  "Add Friend handler to routes"
  [routes-set]
  (handler/site
        (friend/authenticate routes-set friend-settings)))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (friend-routes FrienduiStorageImpl)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
     (wrap-defaults site-defaults)
     (authenticate-routes)))