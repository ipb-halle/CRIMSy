/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

window.onload = function () {
    colorCloudTerms();
};

function colorCloudTerms() {
    var TIME_VARIANCE = 250;
    var TIME_SCALE_FACTOR = 750;
    var tags = $(".ui-tagcloud ul li");
    for (var i = 0; i < tags.length; i++) {
        tags[i].children[0].style.opacity = 0.0;
        var timeScalebyPosition = Math.abs(i - tags.length / 2) / (tags.length / 2);
        var time = timeScalebyPosition * TIME_SCALE_FACTOR + Math.random() * TIME_VARIANCE;
        setTimeout(timeOut, Math.floor(time), tags[i]);
    }

}

function timeOut(tag) {
    var color = (["#a37ed0", "#e11", "#44f", "#d0c020"])[Math.floor(Math.random() * 4)];
    tag.children[0].style.opacity = 1.0;
    tag.children[0].style.color = color;
}


