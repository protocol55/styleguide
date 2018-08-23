(ns protocol55.styleguide.main.client.app
  (:require [protocol55.styleguide.main.client.state :refer [state]]
            [protocol55.styleguide.main.client.components :refer [component]]
            [protocol55.styleguide.main.client.routes :as routes]
            [protocol55.styleguide.alpha :as sg]))

(defn app []
  (let [{:keys [styleguide page-id page-params overview-html] :as page-props} @state]
    [component :page page-props
     (case page-id
       :reference
       (let [{:keys [reference]} page-params
             doc (sg/get-section-by-reference styleguide reference)]
         [component :kss-documentation {:doc doc :styleguide styleguide}])

       [component :overview overview-html])]))
