(ns protocol55.styleguide.main.client.components
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [protocol55.styleguide.main.client.routes :as routes]
            [protocol55.styleguide.main.client.sections :as sections]
            [protocol55.styleguide.alpha :as sg]
            cljsjs.tinycolor
            goog.object))

(declare component)

(defn modified-markup [markup modifier-class]
  (s/replace markup #"(\{\{modifier_class\}\}|\{\$modifiers\})"
             (s/replace (or modifier-class "") #"\." " ")))

(defn set-inner-html! [el html]
  (goog.object/set el "innerHTML" html))

(defn html-outlet
  [_ _]
  (let [set-html! #(some-> %1 (set-inner-html! %2))]
    (fn [tag html]
      [tag {:ref #(set-html! % html)}])))

(defn modifier-example
  [{:keys [markup modifier-name]} ]
  [html-outlet :output.c-psg-modifier__output
   (modified-markup markup modifier-name)])

(defn modifier-block [markup {:keys [name description] :as modifier}]
  (let [display-name (or name "Default")]
    [:div.c-psg-modifier {:key display-name}
     [:div.c-psg-modifier__name display-name " "
      [:p.c-psg-modifier__description (some->> description (str " - "))]]
     [modifier-example {:markup markup :modifier-name name}]]))

(defn light? [color]
  (-> (js/tinycolor color) (.isLight)))

;; -------------------------
;; DEFAULT COMPONENTS

(defn default-stability-tag [{:keys [deprecated? experimental?]}]
  [:span.c-protocol55-styleguide-tag.u-protocol55-styleguide-bg-tint
   {:class (cond
             deprecated?   "c-protocol55-styleguide-tag--deprecated"
             experimental? "c-protocol55-styleguide-tag--experimental"
             :else         "c-protocol55-styleguide-tag--stable")}])

(defn default-parameters [{:keys [parameters]}]
  [:div.c-psg-params
   [:div.c-psg-params__cell.c-psg-params__cell--column.c-psg-params__cell--name
    "Parameter"]
   [:div.c-psg-params__cell.c-psg-params__cell--column.c-psg-params__cell--default 
    "Default"]
   [:div.c-psg-params__cell.c-psg-params__cell--column.c-psg-params__cell--description
    "Description"]
   (map (fn [{:keys [name description default-value] :as parameter}]
          [:<> {:key name}
           [:div.c-psg-params__cell.c-psg-params__cell--name
            name]
           [:div.c-psg-params__cell.c-psg-params__cell--default
            default-value]
           [:div.c-psg-params__cell.c-psg-params__cell--description
            description]])
        parameters)])

(defn default-kss-documentation
  [{{:keys [section-id reference header description modifiers parameters
            source compatibility deprecated? experimental? markup colors]
     :as doc} :doc
    :keys [styleguide]}]
  [:section.c-psg-section
   [:header
    [(if styleguide
       :h1.c-psg-section__heading.c-psg-section__heading--section
       :h2.c-psg-section__heading.c-psg-section__heading--subsection)
     (if header
       [:<>
        (when styleguide
          [:small "Section " section-id])
        header]
       [:<> "Section " section-id])
     " "
     [component :stability-tag (select-keys doc [:deprecated? :experimental?])]]]

   (when description
     [(if styleguide
        :p.c-psg-section__description.c-psg-section__description--intro
        :p.c-psg-section__description)
      description])

   (cond
     markup
     [:<>
      (if (empty? modifiers)
        [modifier-block markup]
        (map (partial modifier-block markup) modifiers))
      [:details.c-psg-section__markup.c-psg-markup
       [:summary "Markup"]
       [:pre [:code markup]]]]

     (seq parameters)
     [default-parameters {:parameters parameters}]

     (seq colors)
     [:dl.c-psg-colors
      (map (fn [{:keys [name description css/color]}]
             [:<> {:key color}
              [:div
               [:dt.c-psg-colors__swatch
                {:style {:background-color color}
                 :class (when (light? color) "c-psg-colors__swatch--bordered")}]
               [:dd.c-psg-colors__name name]
               [:dd.c-psg-colors__color color]
               [:dd.c-psg-colors__description description]]])
           colors)])

   (when compatibility
     [:p.c-psg-section__parameter-compatibility compatibility])

   (when styleguide
     (map (fn [doc]
            ^{:key (:reference doc)} [component :kss-documentation {:doc doc}])
          (sections/tertiary-sections-seq reference styleguide)))])

(defn default-top-navigation [{:keys [sections page-id]}]
  [:nav.c-psg-top-navigation
   [:ul.c-psg-top-navigation__links
    [:li.c-psg-top-navigation__link-item
     [:a.c-psg-top-navigation__link
          {:href (routes/overview-url)}
          "Overview" [:small]]]
    (map (fn [{:keys [reference header section-id] :as doc}]
           [:li.c-psg-top-navigation__link-item {:key reference}
            [:a.c-psg-top-navigation__link
             {:href (routes/reference-url {:reference reference})} header
             [:small section-id ".0"]]])
         sections)]])

(defn default-side-navigation [{:keys [sections page-id page-params]}]
  [:nav.c-psg-side-navigation
    [:ul.c-psg-side-navigation__links
     (map (fn [{:keys [reference header] :as doc}]
            [:li.c-psg-side-navigation__link-item
             {:key reference}
             [:a.c-psg-side-navigation__link
              {:href (routes/reference-url {:reference reference})
               :class (when (= (:reference page-params) reference)
                        "c-psg-side-navigation__link--active")}
              header]])
          sections)]])

(defn default-overview [overview-html]
  [:section.c-psg-markdown
   (if overview-html
     [html-outlet :div overview-html]
     [:<>
      [:h1 "Overview"]
      [:p "To show an overview for your style guide first create a markdown file with the
          content you want displayed here. Then add " [:code "--overview MY_OVERVIEW.md"]
          " to the style guide cli command when you run it."]])])

(defn- side-navigation-partial [sections page-params]
  [component :side-navigation
   {:sections sections
    :page-params page-params}])

(defn default-page [{:keys [styleguide page-id page-params] :as props} content]
  [:div.c-psg-page
   [:header.c-psg-page__header
    [component :top-navigation
             {:sections (sections/primary-sections-seq styleguide)}]]
   [:main.c-psg-page__content
    content]
   [:div.c-psg-page__sidenav
    (some-> (:reference page-params)
            (sections/secondary-sections-seq styleguide)
            seq
            (side-navigation-partial page-params))]])

(def default-components
  {:top-navigation #'default-top-navigation
   :side-navigation #'default-side-navigation
   :kss-documentation #'default-kss-documentation
   :stability-tag #'default-stability-tag
   :overview #'default-overview
   :page #'default-page})

;; This little bit of indirection is to eventually support user overrides of the
;; theme components.
(defmulti component (fn [k & _] k))

(defmethod component :default [k props & children]
  (into [(get default-components k) props] children))
