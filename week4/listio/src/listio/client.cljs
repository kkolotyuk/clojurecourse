(ns my.namespace
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET]])
  (:require-macros [enfocus.macros :as em]))

(em/defsnippet header :compiled "public/prototype/login.html" ["#header"] [])
(em/defsnippet login :compiled "public/prototype/login.html" ["#login-form"] [])

(em/defaction init []
    ["body"] (ef/do-> (ef/content (header))
                      (ef/append (login))))

(set! (.-onload js/window) init)
