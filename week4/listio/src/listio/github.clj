(ns listio.github
  (:require [tentacles.core :refer [api-call]]
            [tentacles.users :as users]
            [tentacles.repos :as repos]
            [tentacles.issues :as issues]
            [clojure.set :refer [subset? union]]))

(def label1 "clear")
(def label2 "not clear")
(def label3 "important")
(def label4 "not important")

(def box1 #{label3 label2})
(def box2 #{label4 label2})
(def box3 #{label4 label1})
(def box4 #{label3 label1})
(def default #{})


(defn success? [resp]
  (and (not (nil? resp))
       (not (contains? #{404 403} (:status resp)))))

(defn fetch-user [access-token]
  (users/me {:oauth_token access-token}))

(defn fetch-repo [username repo]
  (let [resp (repos/specific-repo username repo)]
    (when (success? resp)
      resp)))

(defn which-box [issue]
  (let [labels (set (:labels issue))]
    (condp subset? labels
      box1 1
      box2 2
      box3 3
      box4 4
      #{}  "default")))

(defn fetch-issue-info [issue username repo]
  (let [info (select-keys issue [:login :title :body :number :html_url])
        labels (map :name (:labels issue))
        assignee (get-in issue [:assignee :login])
        additional-info {:assignee assignee
                         :labels labels
                         :username username
                         :repo repo}]
    (merge info additional-info)))

(defn fetch-open-issues [username reponame]
  (let [repo (fetch-repo username reponame)
        repo-issues (cond
                      (nil? repo) nil
                      (> (:open_issues_count repo) 0) (issues/issues username reponame)
                      :else [])]
    (when (success? repo-issues)
      (let [issues-info (map #(fetch-issue-info % username reponame) repo-issues)
            grouped-issues (group-by which-box issues-info)]
        grouped-issues))))

(defn fetch-issue [username repo number]
  (let [issue (issues/specific-issue username repo number)]
    (when (success? issue)
      (fetch-issue-info issue username repo))))

(defn clear-labels [labels]
  (disj labels label1 label2 label3 label4))

(defn to-the-box [access-token username repo number box]
  (when-let [issue (fetch-issue username repo number)]
    (let [box (get {"1" box1 "2" box2 "3" box3 "4" box4 "default" default} box)
          cleared-labels (clear-labels (set (:labels issue)))
          boxed-labels (union box cleared-labels)
          new-labels (apply list boxed-labels)
          resp (issues/edit-issue username repo number {:oauth_token access-token :labels new-labels})]
      (if (success? resp)
        {:success true}
        {:success false :message "Something went wrong. May be you don't have permissions to change these issues."})
      )))


