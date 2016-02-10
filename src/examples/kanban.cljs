(ns examples.kanban)

(def kanban-norm-state
  {:lanes
            [[:lane/by-id 10]
             [:lane/by-id 11]
             [:lane/by-id 12]
             [:lane/by-id 13]
             [:lane/by-id 14]
             [:lane/by-id 15]],
   :secrets [[:secret/by-id 2000] [:secret/by-id 2001]],
   :secret/by-id
            {2000 {:id 2000, :name "First Secret"},
             2001 {:id 2001, :name "Second Secret"}},
   :card/by-id
            {102 {:id 102, :text "Fix card drag-and-drop on IE8 and Firefox"},
             100 {:id 100, :text "Allow users to edit card assignees"},
             101 {:id 101, :text "Allow users to change lane names"},
             103
                 {:id        103,
                  :text      "Implement basic Kanban demo app",
                  :assignees [[:user/by-id 1000]]},
             105
                 {:id        105,
                  :text      "Write instructions on how to run the demo app",
                  :assignees [[:user/by-id 1001]]},
             104
                 {:id        104,
                  :text      "Figure out how to deploy the demo app on Heroku",
                  :assignees [[:user/by-id 1000] [:user/by-id 1001]]},
             106 {:id 106, :text "Create GitHub repository for the demo app"}},
   :cards
            [[:card/by-id 100]
             [:card/by-id 101]
             [:card/by-id 102]
             [:card/by-id 103]
             [:card/by-id 104]
             [:card/by-id 105]
             [:card/by-id 106]],
   :lane/by-id
            {10 {:id 10, :name "Issues", :cards [[:card/by-id 102]]},
             11
                {:id    11,
                 :name  "Backlog",
                 :cards [[:card/by-id 100] [:card/by-id 101]]},
             12 {:id 12, :name "Doing", :cards [[:card/by-id 103]]},
             13 {:id 13, :name "Test", :cards [[:card/by-id 105]]},
             14 {:id 14, :name "Testing", :cards [[:card/by-id 104]]},
             15 {:id 15, :name "Done", :cards [[:card/by-id 106]]}},
   :user/by-id
            {1000 {:id 1000, :username "konrad", :name "Konrad Zuse"},
             1001 {:id 1001, :username "ada", :name "Ada Lovelace"}},
   :board/by-id
            {1
             {:id     1,
              :name   "Development",
              :description
                      "Kanban board for developers. Developers select work from Issues and the Backlog. When they start working, they move these cards to doing and assign them to themselves. Once completed, they move the card to Test for testers to verify the results of the work.",
              :lanes
                      [[:lane/by-id 10]
                       [:lane/by-id 11]
                       [:lane/by-id 12]
                       [:lane/by-id 13]],
              :secret [:secret/by-id 2000]},
             2
             {:id   2,
              :name "Testing",
              :description
                    "Kanban board for testers. Testers pick up the work that developers have completed and moved to Test. They then move these cards to Testing and assign them to themselves. If testing is successful, cards are moved to Done, otherwise they are moved to Issues, where they are again picked up by developers for another round.",
              :lanes
                    [[:lane/by-id 13]
                     [:lane/by-id 14]
                     [:lane/by-id 15]
                     [:lane/by-id 10]]}},
   :boards  [[:board/by-id 1] [:board/by-id 2]],
   :users   [[:user/by-id 1000] [:user/by-id 1001]]})

(def kanban-corrected-norm-state
  {:kanban/lanes
            [[:lane/by-id 10]
             [:lane/by-id 11]
             [:lane/by-id 12]
             [:lane/by-id 13]
             [:lane/by-id 14]
             [:lane/by-id 15]],
   :kanban/secrets [[:secret/by-id 2000] [:secret/by-id 2001]],
   :secret/by-id
            {2000 {:id 2000, :name "First Secret"},
             2001 {:id 2001, :name "Second Secret"}},
   :card/by-id
            {102 {:id 102, :text "Fix card drag-and-drop on IE8 and Firefox"},
             100 {:id 100, :text "Allow users to edit card assignees"},
             101 {:id 101, :text "Allow users to change lane names"},
             103
                 {:id        103,
                  :text      "Implement basic Kanban demo app",
                  :assignees [[:user/by-id 1000]]},
             105
                 {:id        105,
                  :text      "Write instructions on how to run the demo app",
                  :assignees [[:user/by-id 1001]]},
             104
                 {:id        104,
                  :text      "Figure out how to deploy the demo app on Heroku",
                  :assignees [[:user/by-id 1000] [:user/by-id 1001]]},
             106 {:id 106, :text "Create GitHub repository for the demo app"}},
   :kanban/cards
            [[:card/by-id 100]
             [:card/by-id 101]
             [:card/by-id 102]
             [:card/by-id 103]
             [:card/by-id 104]
             [:card/by-id 105]
             [:card/by-id 106]],
   :lane/by-id
            {10 {:id 10, :name "Issues", :cards [[:card/by-id 102]]},
             11
                {:id    11,
                 :name  "Backlog",
                 :cards [[:card/by-id 100] [:card/by-id 101]]},
             12 {:id 12, :name "Doing", :cards [[:card/by-id 103]]},
             13 {:id 13, :name "Test", :cards [[:card/by-id 105]]},
             14 {:id 14, :name "Testing", :cards [[:card/by-id 104]]},
             15 {:id 15, :name "Done", :cards [[:card/by-id 106]]}},
   :user/by-id
            {1000 {:id 1000, :username "konrad", :name "Konrad Zuse"},
             1001 {:id 1001, :username "ada", :name "Ada Lovelace"}},
   :board/by-id
            {1
             {:id     1,
              :name   "Development",
              :description
                      "Kanban board for developers. Developers select work from Issues and the Backlog. When they start working, they move these cards to doing and assign them to themselves. Once completed, they move the card to Test for testers to verify the results of the work.",
              :lanes
                      [[:lane/by-id 10]
                       [:lane/by-id 11]
                       [:lane/by-id 12]
                       [:lane/by-id 13]],
              :secret [:secret/by-id 2000]},
             2
             {:id   2,
              :name "Testing",
              :description
                    "Kanban board for testers. Testers pick up the work that developers have completed and moved to Test. They then move these cards to Testing and assign them to themselves. If testing is successful, cards are moved to Done, otherwise they are moved to Issues, where they are again picked up by developers for another round.",
              :lanes
                    [[:lane/by-id 13]
                     [:lane/by-id 14]
                     [:lane/by-id 15]
                     [:lane/by-id 10]]}},
   :kanban/boards  [[:board/by-id 1] [:board/by-id 2]],
   :kanban/users   [[:user/by-id 1000] [:user/by-id 1001]]})