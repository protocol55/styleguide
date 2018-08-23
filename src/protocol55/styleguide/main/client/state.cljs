(ns protocol55.styleguide.main.client.state
  (:require [reagent.core :as r]))

(defonce state (r/atom nil))

(defn set-page-info!
  ([page-id]
   (set-page-info! page-id nil))
  ([page-id page-params]
   (swap! state assoc :page-id page-id :page-params page-params)))
