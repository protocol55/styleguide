(ns protocol55.styleguide.main.client.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [protocol55.styleguide.main.client.state :refer [set-page-info!]])
  (:import goog.History))

(defn scroll-top! []
  (.scrollTo js/window 0 0))

(secretary/set-config! :prefix "#")

(defroute overview-url "/" []
  (scroll-top!)
  (set-page-info! :overview))

(defroute reference-url "/reference/:reference" [reference]
  (scroll-top!)
  (set-page-info! :reference {:reference reference}))

(defn start! []
  (let [h (History.)]
    (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true))))
