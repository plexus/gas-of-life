(ns gasoflife.core
  (:require [gasoflife.entry-points]))

(def sheet-app js/SpreadsheetApp)

(defn active-spreadsheet []
  (.getActive sheet-app))

(defn active-sheet []
  (.getActiveSheet sheet-app))

(defn spreadsheet-ui []
  (.getUi sheet-app))

(def ROWS 10)
(def COLS 10)
(def CELLSIZE 20)

(def white "#ffffff")
(def black "#000000")

(defn map-cells [f]
  (mapv (fn [r]
          (mapv (fn [c]
                  (f r c))
                (range COLS)))
        (range ROWS)))

(defn initial-state []
  (map-cells (fn [r c]
               (rand-nth [true false]))))

(defn get-cell-color [r c]
  (= black (.getBackground (.getRange (active-sheet) (inc r) (inc c) 1 1))))

(defn set-cell-color [r c b]
  (.setBackground (.getRange (active-sheet) (inc r) (inc c) 1 1)
                  (if b
                    black
                    white)))

(defn read-state []
  (map-cells get-cell-color))

(defn neighbors [[x y]]
  (remove (partial = [x y])
          (for [xv [1 0 -1]
                yv [1 0 -1]]
            [(+ x xv) (+ y yv)])))

(defn render [grid]
  (map-cells #(set-cell-color %1 %2 (get-in grid [%1 %2]))))

(defn step [grid]
  (map-cells (fn [row col]
               (let [cell [row col]
                     live (get-in grid cell)
                     neighbor-coords (neighbors cell)
                     neighbor-count (count (filter #(get-in grid %) neighbor-coords))]
                 (if live
                   (<= 2 neighbor-count 3)
                   (= neighbor-count 3))))))

(defn create-menu! []
  (.. (spreadsheet-ui)
      (createMenu "Game of Life")
      (addItem "Start", "onOpen")
      (addItem "Step", "step")
      (addToUi)))

(defn resize-cells! []
  (let [sheet (active-spreadsheet)]
    (run! (fn [i] (.setRowHeight sheet (inc i) CELLSIZE)) (range ROWS))
    (run! (fn [i] (.setColumnWidth sheet (inc i) CELLSIZE)) (range COLS))))

(defn ^:export start []
  (create-menu!)
  (resize-cells!)
  (render (initial-state))
  (let [state (read-state)]
    (map-cells
     #(js/Logger.log (get-in state [%1 %2])))))

(defn ^:export do-step []
  (let [state (read-state)
        next (step state)]
    (js/Logger.log (pr-str state))
    (js/Logger.log (pr-str next))
    (render next)))
