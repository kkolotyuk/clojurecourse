(ns my.namespace
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(em/defsnippet login-header :compiled "public/prototype/login.html" ["body"] [])

(em/defaction init []
    ["body"] (ef/content (login-header)))

(set! (.-onload js/window) init)
