(ns protocol55.styleguide.main.client.core
  (:require [reagent.core :as r]
            protocol55.styleguide.main.kss-reload
            protocol55.styleguide.main.overview-reload
            [protocol55.styleguide.main.client.routes :as routes]
            [protocol55.styleguide.main.client.app :refer [app]]))

(defonce root-node (.getElementById js/document "app"))

(defn mount-root []
  (r/unmount-component-at-node root-node)
  (r/render [app] root-node))

(defn run []
  (mount-root))

(run)

(defonce history (routes/start!))
