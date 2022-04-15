(ns waddams.core
  (:require [clojure.java.io :as io]
            [waddams.csv :as csv]
            [waddams.transaction :as transaction]))

(def all-transactions-file-path "waddams-output/all-transactions.csv")

(defn write-completed-ticker-transactions! [completed-grouped-transactions]
  (doseq [ticker (keys completed-grouped-transactions)
          :let [file-path (format "waddams-output/%s.csv" ticker)]]
    (println (format "Writing %s" file-path))
    (->> (get completed-grouped-transactions ticker)
         (csv/write-completed-transactions! file-path))))

(defn write-all-transactions! [completed-combined-transactions]
  (println (format "Writing %s" all-transactions-file-path))
  (csv/write-completed-transactions! all-transactions-file-path
                                     completed-combined-transactions))

(defn -main [& args]
  (with-precision 10
    (let [{:keys [close! transactions]} (-> args first csv/read-transactions)
          completed-grouped-transactions
          (transaction/completed-grouped-transactions transactions)
          completed-combined-transactions (->> completed-grouped-transactions
                                               vals
                                               (apply concat)
                                               (sort-by :ts))]
      (io/make-parents all-transactions-file-path)
      (write-completed-ticker-transactions! completed-grouped-transactions)
      (write-all-transactions! completed-combined-transactions)
      (close!))))
