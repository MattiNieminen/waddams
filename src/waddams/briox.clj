(ns waddams.briox
  (:require [waddams.csv :as csv]
            [waddams.webdriver :as webdriver])
  (:import (java.time.format DateTimeFormatter)))

(def wait-time-for-inputs 500)

(def buy-account-id "4210")
(def sell-account-id "3460")
(def costs-account-id "8550")
(def cash-account-id "1920")

(def date-time-formatter (DateTimeFormatter/ofPattern "dd.MM.yyyy"))

(defn navigate-to-login-view! [wd]
  (webdriver/open-url! wd "https://fi.briox.services/"))

(defn open-accouting-view! [wd]
  (-> wd
      (webdriver/find-elements-by-css "#top_menu_link-accounting")
      first
      (webdriver/click!))
  (-> wd
      (webdriver/find-elements-by-css "#accounting_journals > a")
      first
      (webdriver/click!)))

(defn add-voucher! [wd {:keys [id
                               time
                               type
                               transaction-amount
                               transaction-costs]}]
  (let [buy? (= type :buy)
        costs? (-> transaction-costs zero? not)
        transaction-amount-with-costs (+ transaction-amount transaction-costs)]
    (-> wd
        (webdriver/find-elements-by-css "#create")
        first
        (webdriver/click!))
    (doseq [[css text]
            (remove nil? [["#descr"
                           (format "%s %s" (if buy? "Osto" "Myynti") id)]
                          ["#tdate" (.format date-time-formatter time)]
                          ["#account_0" cash-account-id]
                          [(if buy? "#cre_0" "#deb_0")
                           (if buy?
                             (str transaction-amount-with-costs)
                             (str transaction-amount))]
                          ["#account_1"
                           (if buy? buy-account-id sell-account-id)]
                          [(if buy? "#deb_1" "#cre_1")
                           (if buy?
                             (str transaction-amount)
                             (str transaction-amount-with-costs))]
                          (when costs?
                            ["#account_2" costs-account-id])
                          (when costs?
                            ["#deb_2" (str transaction-costs)])])
            :let [el (-> wd
                         (webdriver/find-elements-by-css css)
                         first)]]
      (webdriver/setText! el text)
      (Thread/sleep wait-time-for-inputs)
      (webdriver/pressEnter! el)
      (Thread/sleep wait-time-for-inputs))

    ;; Comment for testing
    (-> wd
        (webdriver/find-elements-by-css "#button_save_voucher")
        first
        (webdriver/click!))
    (Thread/sleep wait-time-for-inputs)))

(defn -main [& args]
  (let [{:keys [close! transactions]} (-> args first csv/read-transactions)
        wd (webdriver/open-webdriver! true)]
    (navigate-to-login-view! wd)
    (Thread/sleep 20000)
    (open-accouting-view! wd)
    (doseq [transaction transactions]
      (add-voucher! wd transaction))
    (webdriver/quit-webdriver! wd)
    (close!)))
