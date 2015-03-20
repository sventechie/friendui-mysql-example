(ns friendui-mysql-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [cemerick.friend :as friend]
            [cemerick.friend [workflows :as workflows]
                             [credentials :as creds]]
            [de.sveri.friendui.routes.user :refer [friend-routes]]
            [sventechie.friendui-mysql.db :refer :all]
            [sventechie.friendui-mysql.storage :refer :all])
  (:use [sventechie.friendui-mysql.storage FrienduiStorage]))

(def FrienduiStorageImpl (->FrienduiStorage))
(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn
                                       (get-all-users (FrienduiStorageImpl)))
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
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
