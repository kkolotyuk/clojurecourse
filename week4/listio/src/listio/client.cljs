(ns my.namespace
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(defn start []
  (ef/at js/document
    ["body"] (ef/content "Hello enfocus!")))

(set! (.-onload js/window) start)
