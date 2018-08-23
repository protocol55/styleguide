(ns protocol55.styleguide.tests.alpha
  (:require [protocol55.styleguide.alpha :refer :all])
  (:use clojure.test))

(deftest test-styleguide
  (is (= nil (styleguide)))
  (is (= nil (styleguide (repeat 5 {}))))
  (is (= 1 (count (styleguide [{:reference "foo.bar"}])))))

(deftest test-reference-key
  (is (= [1] (reference-key "1")))
  (is (= [42] (reference-key "42")))
  (is (= [1] (reference-key "1.0.0.0")))
  (is (= ["A" "B"] (reference-key "A.B"))))

(deftest test-weighted-reference-key
  (is (= [[0.0 1] [0.0 2] [0.0 3]] (weighted-reference-key {} [1 2 3])))
  (is (= [[2.0 1] [0.0 2] [0.0 3]] (weighted-reference-key {1 {1 2.0}} [1 2 3]))))

(deftest test-weighted-reference-key-pad
  (is (= 0 (weighted-reference-key-pad [[0.0 1]])))
  (is (= "" (weighted-reference-key-pad [[0.0 "A"]]))))

(deftest test-weighted-reference-key-compare
  (is (= [[1] [1 2] [2] [2 1]]
         (->> [[[0.0 2]] [[0.0 2] [0.0 1]] [[0.0 1]] [[0.0 1] [0.0 2]]]
              (sort weighted-reference-key-compare)
              (map #(mapv second %)))))
  (is (= [[2] [2 1] [1] [1 2]]
         (->> [[[0.0 2]] [[0.0 2] [0.0 1]] [[1.0 1]] [[1.0 1] [0.0 2]]]
              (sort weighted-reference-key-compare)
              (map #(mapv second %))))))

(deftest test-base-ref-depth
  (is (= 1 (base-ref-depth [1] [2 1])))
  (is (= 3 (base-ref-depth [1 1] [1 1 2]))))

(deftest test-add
  (is (= nil (add nil nil)))
  (is (= {1 2.0 2 3.0}
         (-> (add (styleguide [{:reference "1" :weight 1.0} {:reference "2" :weight 3.0}])
                  [{:reference "1" :weight 2.0}])
             meta :protocol55.styleguide.alpha/weight-map (get 1)))))

(deftest test-sections-seq
  (let [numeric-docs
        [{:reference "1.0"}
         {:reference "2.0"}
         {:reference "2.1"}
         {:reference "2.1.1"}
         {:reference "2.2"}
         {:reference "2.2.1"}
         {:reference "2.2.2"}
         {:reference "2.2.3"}
         {:reference "1.1"}
         {:reference "1.1.1"}]
        named-docs
        [{:reference "Components.Buttons.Primary"}
         {:reference "Components.Buttons"}
         {:reference "Settings"}
         {:reference "Tools"}
         {:reference "Tools.Textarea"}
         {:reference "Components"}
         {:reference "Tools.IE"}]
        weighted-named-docs
        [{:reference "Components.Buttons.Primary"}
         {:reference "Components.Buttons" :weight 3.0}
         {:reference "Components.Menus" :weight 1.0}
         {:reference "Components.Cards" :weight 2.0}
         {:reference "Settings" :weight 1.0}
         {:reference "Tools" :weight 2.0}
         {:reference "Components" :weight 3.0} ]]

    ;; numeric reference sorting

    (is (= ["1.0" "1.1" "1.1.1" "2.0" "2.1" "2.1.1" "2.2" "2.2.1" "2.2.2" "2.2.3"]
           (mapv :reference (sections-seq (styleguide numeric-docs)))))
    (is (= ["1.0" "1.1" "1.1.1"]
           (mapv :reference (sections-seq [1] (styleguide numeric-docs)))))
    (is (= ["2.1" "2.1.1"]
           (mapv :reference (sections-seq [2 1] (styleguide numeric-docs)))))
    (is (= ["1.1" "1.1.1" "2.1" "2.1.1" "2.2" "2.2.1" "2.2.2" "2.2.3"]
           (mapv :reference (sections-seq [] 2 3 (styleguide numeric-docs)))))
    (is (= ["2.1.1"]
           (mapv :reference (sections-seq [2 1] 3 3 (styleguide numeric-docs)))))
    (is (= ["1.0" "2.0"]
           (mapv :reference (sections-seq [] 1 (styleguide numeric-docs)))))
    (is (= ["2.1" "2.1.1"]
           (mapv :reference (sections-seq [2 1] 5000 (styleguide numeric-docs)))))

    ;; numeric sections
    (is (= ["1" "1.1" "1.1.1" "2" "2.1" "2.1.1" "2.2" "2.2.1" "2.2.2" "2.2.3"]
           (mapv :section-id (sections-seq (styleguide numeric-docs)))))
    (is (= ["1" "1.1" "1.1.1"]
           (mapv :section-id (sections-seq [1] (styleguide numeric-docs)))))
    (is (= ["2.1" "2.1.1"]
           (mapv :section-id (sections-seq [2 1] (styleguide numeric-docs)))))
    (is (= ["1.1" "1.1.1" "2.1" "2.1.1" "2.2" "2.2.1" "2.2.2" "2.2.3"]
           (mapv :section-id (sections-seq [] 2 3 (styleguide numeric-docs)))))
    (is (= ["2.1.1"]
           (mapv :section-id (sections-seq [2 1] 3 3 (styleguide numeric-docs)))))
    (is (= ["1" "2"]
           (mapv :section-id (sections-seq [] 1 (styleguide numeric-docs)))))
    (is (= ["2.1" "2.1.1"]
           (mapv :section-id (sections-seq [2 1] 5000 (styleguide numeric-docs)))))

    ;; named reference sorting
    (is (= ["Components" "Components.Buttons" "Components.Buttons.Primary" "Settings" "Tools" "Tools.IE" "Tools.Textarea"]
           (mapv :reference (sections-seq (styleguide named-docs)))))
    (is (= ["Components" "Components.Buttons" "Components.Buttons.Primary"]
           (mapv :reference (sections-seq ["Components"] (styleguide named-docs)))))
    (is (= ["Components.Buttons" "Components.Buttons.Primary"]
           (mapv :reference (sections-seq ["Components" "Buttons"] (styleguide named-docs)))))
    (is (= ["Components.Buttons" "Components.Buttons.Primary" "Tools.IE" "Tools.Textarea"]
           (mapv :reference (sections-seq [] 2 3 (styleguide named-docs)))))
    (is (= ["Components.Buttons.Primary"]
           (mapv :reference (sections-seq ["Components" "Buttons"] 3 3 (styleguide named-docs)))))
    (is (= ["Components" "Settings" "Tools"]
           (mapv :reference (sections-seq [] 1 (styleguide named-docs)))))

    ;; named sections
    (is (= ["1" "1.1" "1.1.1" "2" "3" "3.1" "3.2"]
           (mapv :section-id (sections-seq (styleguide named-docs)))))
    (is (= ["1" "1.1" "1.1.1"]
           (mapv :section-id (sections-seq ["Components"] (styleguide named-docs)))))
    (is (= ["1.1" "1.1.1"]
           (mapv :section-id (sections-seq ["Components" "Buttons"] (styleguide named-docs)))))
    (is (= ["1.1" "1.1.1" "3.1" "3.2"]
           (mapv :section-id (sections-seq [] 2 3 (styleguide named-docs)))))
    (is (= ["1.1.1"]
           (mapv :section-id (sections-seq ["Components" "Buttons"] 3 3 (styleguide named-docs)))))
    (is (= ["1" "2" "3"]
           (mapv :section-id (sections-seq [] 1 (styleguide named-docs)))))

    ;; weighted
    (is (= ["Settings" "Tools" "Components"
            "Components.Menus" "Components.Cards" "Components.Buttons" "Components.Buttons.Primary"]
           (mapv :reference (sections-seq (styleguide weighted-named-docs)))))))
