(ns waddams.webdriver
  (:import (io.github.bonigarcia.wdm WebDriverManager)
           (java.util.concurrent TimeUnit)
           (org.openqa.selenium By Keys)
           (org.openqa.selenium.chrome ChromeDriver ChromeOptions)))

(defn open-webdriver! [incognito?]
  (.setup (WebDriverManager/chromedriver))
  (let [options (ChromeOptions.)
        _ (when incognito?
            (.addArguments options (java.util.ArrayList. ["-private"])))
        _ (.addArguments options (java.util.ArrayList. ["--disable-dev-shm-usage"]))
        wd (ChromeDriver. options)]
    (-> wd .manage .timeouts (.implicitlyWait 5, TimeUnit/SECONDS))
    wd))

(defn quit-webdriver! [wd]
  (.quit wd))

(defn open-url! [wd url]
  (.get wd url))

(defn find-elements-by-css [wd css]
  (.findElements wd (By/cssSelector css)))

(defn click! [el]
  (.click el))

(defn sendKeys! [el s-key-or-chord]
  (.sendKeys el (into-array [s-key-or-chord])))

(defn pressEnter! [el]
  (sendKeys! el Keys/ENTER))

(defn setText! [el s]
  (.clear el)
  (sendKeys! el (Keys/chord [Keys/CONTROL "a"]))
  (sendKeys! el Keys/DELETE)
  (sendKeys! el s))
