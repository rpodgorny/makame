(ns frontend.dbs)

(def users #{{:first-name "Radek"
              :last-name "Podgorny"
              :id "rpodgorny"
              :email "radek@podgorny.cz"}
             {:first-name "Pavel"
              :last-name "Podgorny"
              :id "ppodgorny"}
             {:first-name "Ilona"
              :last-name "Belovova"
              :id "ibelovova"}
             {:first-name "Stepan"
              :last-name "Donath"
              :id "sdonath"}
             {:first-name "Tomas"
              :last-name "Malik"
              :id "tmalik"}})

(def tasks [{:id 123
             :text "telefonat na mj666"
             :mentor "rpodgorny"
             :worker "tmalik"
             :estimate 3600
             :deadline "2019-02-07"
             :tags ["billable" "manual"]
             :duration 1000
             :started-at 1548094583}
            {:id 234
             :text "papirovani"
             :mentor "ppodgorny"
             :worker "ibelovova"
             :estimate 8000
             :deadline "2019-07-09"
             :so-far 2000
             :started-at 1548094583}
            {:id 234555
             :text "papirovani"
             :mentor "ppodgorny"
             :worker "ibelovova"
             :estimate 8000
             :deadline "2019-07-09"}
            {:id 1234
             :text "telefonat na mj667"
             :estimate 3600
             :deadline "2019-01-01"
             :duration 3000
             :finished-at 1548094583
             :tags ["billable" "manual"]}
            {:id 444
             :text "kresleni projektu mj77"
             :mentor "sdonath"
             :worker "sdonath"
             :estimate 3600
             :deadline "2019-03-05"
             :started-at 1548094583}])
