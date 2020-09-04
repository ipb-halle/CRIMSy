{ "comment" : "single filter chain to transform a document into text",
  "type" : "FilterDriver",
  "chains" : [ {
        "comment" : "First chain",
        "chain" : [ {
            "type" : "AggregatingFilter",
            "aggregationLength" : 100000
            },{
            "type" : "PatternFilter",
            "patternConfig" : [ {
                    "comment" : "remove control characters",
                    "pattern" : "\\p{Cntrl}",
                    "replacement" : ""
                }, {
                    "comment" : "remove short sequences of digits",
                    "pattern" : " \\d{1,3}",
                    "replacement" : " "
                }, {
                    "comment" : "remove sequences of whitespace",
                    "pattern" : " \\s{2,}",
                    "replacement" : " "
                } ]
            },{
            "type" : "OutputFilter"
            } ]
        } 
    ]
}
