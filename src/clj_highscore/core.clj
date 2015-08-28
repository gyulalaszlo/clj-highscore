(ns clj-highscore.core
  (:gen-class)
  (:require [liberator.core :refer [resource defresource]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.java.jdbc :as db]
            [environ.core :refer [env]]
            [cheshire.core :refer [generate-string]]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]))

(defn all-scores
  [game]
  (db/query (env :database-url)
            ["select * from scores where game = ? " game]))

(defresource get-scores
             [game]
             :allowed-methods [:get]
             :handle-ok (fn [_] (generate-string (vector game (all-scores game))))
             :available-media-types ["application/json"])

(defresource add-score
             :allowed-methods [:post]
             :malformed? (fn [context]
                           (let [params (get-in context [:request :form-params])]
                             (empty? (get params "game"))))
             :handle-malformed "user name cannot be empty!"
             :post!
             (fn [context]
               (let [params (get-in context [:request :form-params])]
                 (println "ADD")))
             :handle-created (fn [_] (generate-string (all-scores "tableau-cnake")))
             :available-media-types ["application/json"])

(defroutes app
           (ANY "/" resource)
           (GET "/get-scores/:game" [game] (get-scores game))

(def handler
  (-> app
      wrap-params))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 80))]
    (jetty/run-jetty (site #'handler) {:port port :join? false})))
