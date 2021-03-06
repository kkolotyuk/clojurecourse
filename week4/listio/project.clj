(defproject listio "0.1.0-SNAPSHOT"
  :description "Github board"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.2.2"]
                 [compojure "1.1.8"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [enfocus "2.1.0-SNAPSHOT"]
                 [tentacles "0.2.7"]
                 [stuarth/clj-oauth2 "0.3.2"]
                 [fogus/ring-edn "0.2.0"]
                 [cljs-ajax "0.2.4"]
                 [jayq "2.5.1"]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.3"]]}}
  :ring {:handler listio.server/app}
  :target-path "target/%s"
  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler {:output-to "resources/public/js/main.js"
                                   :optimizations :simple
                                   :pretty-print true}}]})
