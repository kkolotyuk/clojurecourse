(ns listio.github
  (:require [tentacles.core :refer [api-call]]
            [tentacles.users :as users]
            [tentacles.repos :as repos]
            [tentacles.issues :as issues]
            [clojure.set :refer [subset?]]))

(def label1 "clear")
(def label2 "not clear")
(def label3 "important")
(def label4 "not important")

(def box1 #{label3 label2})
(def box2 #{label4 label2})
(def box3 #{label4 label1})
(def box4 #{label3 label1})


(defn found? [resp]
  (not= 404 (:status resp)))

(defn fetch-user [access-token]
  (users/me {:oauth_token access-token}))

(defn fetch-repo [username repo]
  (let [resp (repos/specific-repo username repo)]
    (when (found? resp)
      resp)))

(defn which-box [issue]
  (let [labels (set (:labels issue))]
    (condp subset? labels
      box1 :box1
      box2 :box2
      box3 :box3
      box4 :box4
      #{}  :default)))

(defn fetch-issue-info [issue]
  (let [info (select-keys issue [:login :title :body :number :html_url])
        labels (map :name (:labels issue))
        assignee (get-in issue [:assignee :login])
        additional-info {:assignee assignee
                         :labels labels}]
    (merge info additional-info)))

(defn fetch-open-issues [username repo]
  (let [repo-issues (issues/issues username repo)]
    (when (found? repo-issues)
      (let [issues-info (map fetch-issue-info repo-issues)
            grouped-issues (group-by which-box issues-info)]
        grouped-issues))))
