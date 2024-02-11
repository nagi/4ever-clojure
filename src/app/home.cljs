(ns app.home
  (:require [app.data :as data]
            [app.state :as state :refer [db]]
            [reagent.core :as r]))

(def user-data (r/cursor db [:solutions]))

(def sort-by-solved (r/cursor db [:sort-by-solved]))

(defn sorted-problems []
  (let [data-state
        (map #(assoc % :solution (get @user-data (:id %)))
             data/problems)
        sorted (if (nil? @sort-by-solved)
                 (sort-by :id data-state)
                 (sort-by :solution #(not (nil? %)) data-state))]
    (if (false? @sort-by-solved) (reverse sorted) sorted)))

(defn get-problem-status [id]
  (let [{:keys [passed failed]}
        (get @user-data (js/parseInt id))
        progress (str passed "/" (+ passed failed))]
    (cond
      (and passed (zero? failed))
      [:span {:style {:color "green"}} (str progress " Passed!")]
      (not (nil? passed)) progress
      :else "-")))

(defn problem-list-item [{:keys [id title tags difficulty]}]
  [:tr
   [:td id]
   [:td
    [:a {:href (state/href :problem/item {:id id})}
     title]]
   [:td difficulty]
   [:td (map str tags)]
   [:td (get-problem-status id)]])

(defn problem-list []
  [:<>
   [:h3 "Problems "
    [:small (str "(" (count data/problems) ")")]]
   (into [:table
          [:thead
           [:tr
            [:th {:on-click #(swap! sort-by-solved (fn [] nil))} "No."]
            [:th "Name"]
            [:th "Difficulty"]
            [:th "Tags"]
            [:th
             {:on-click #(swap! sort-by-solved not)}
             (str "Status  " (case @sort-by-solved
                               true "🠕" false "🠗" nil ""))]]]
          [:tbody
           (for [problem (sorted-problems)]
             ^{:key (:id problem)}
             [problem-list-item problem])]])])

(defn view []
  [:div
   [:p
    "Keeping 4clojure alive forever! This website is completely static and evals
     code using sci. Suggestions / PRs welcome at "
    [:a {:href "https://github.com/oxalorg/4ever-clojure"}
     "github.com/oxalorg/4ever-clojure"]]
   [:p
    "Please note that 4ever-clojure is evaluated completely in the browser. So
    not all Java interop works, but some of it is the same in JS if you're
    lucky. Check "
    [:a {:href "https://cljs.info/cheatsheet/"} "cljs-cheatsheet"]
    " for more info!"]
   [problem-list]])
