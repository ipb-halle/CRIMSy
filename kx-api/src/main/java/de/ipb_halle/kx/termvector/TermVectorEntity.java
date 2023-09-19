/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.termvector;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "termvectors")
@SqlResultSetMapping(name="TermFrequencyResult", 
    classes = {
            @ConstructorResult(
                columns = {
                        @ColumnResult(name="wordroot"), 
                        @ColumnResult(name="frequency", type=Integer.class)},
                targetClass = TermFrequency.class)
            }
)
@SqlResultSetMapping(name="StemmedWordOriginResult", classes = {
    @ConstructorResult(targetClass = StemmedWordOrigin.class,
    columns = {@ColumnResult(name="wordroot"), @ColumnResult(name="original")})
})
@NamedNativeQuery(
    name="SQL_LOAD_TERMFREQUENCY",
    query="SELECT wordroot, termfrequency AS frequency FROM termvectors WHERE file_id=:fileId ORDER BY frequency DESC",
    resultSetMapping="TermFrequencyResult")

@NamedNativeQuery(
    name="SQL_LOAD_TERMFREQUENCY_BY_WORD",
    query="SELECT wordroot, termfrequency AS frequency FROM termvectors WHERE file_id=(:fileId) AND wordroot in(:words) ORDER BY frequency DESC",
    resultSetMapping="TermFrequencyResult")

@NamedNativeQuery(
    name="SQL_LOAD_TERMFREQUENCIES",
    query="SELECT wordroot, SUM(termfrequency) AS frequency FROM termvectors WHERE file_id in (:fileIds) GROUP BY wordroot ORDER BY frequency DESC",
    resultSetMapping="TermFrequencyResult")

@NamedNativeQuery(
    name="SQL_LOAD_UNSTEMMED_WORD",
    query="SELECT stemmed_word AS wordroot, unstemmed_word AS original FROM unstemmed_words WHERE file_id=:fileId AND stemmed_word=:wordroot",
    resultSetMapping="StemmedWordOriginResult")

@NamedNativeQuery(
    name="SQL_LOAD_UNSTEMMED_WORDS",
    query="SELECT stemmed_word AS wordroot, unstemmed_word AS original FROM unstemmed_words WHERE file_id=:fileId AND stemmed_word IN (:wordroot)",
    resultSetMapping="StemmedWordOriginResult")

public class TermVectorEntity implements Serializable {

    public final static String SQL_LOAD_TERMFREQUENCY="SQL_LOAD_TERMFREQUENCY";
    public final static String SQL_LOAD_TERMFREQUENCY_BY_WORD="SQL_LOAD_TERMFREQUENCY_BY_WORD";
    public final static String SQL_LOAD_TERMFREQUENCIES="SQL_LOAD_TERMFREQUENCIES";
    public final static String SQL_LOAD_UNSTEMMED_WORD="SQL_LOAD_UNSTEMMED_WORD";
    public final static String SQL_LOAD_UNSTEMMED_WORDS="SQL_LOAD_UNSTEMMED_WORDS";
    

    @EmbeddedId
    private TermVectorId id;

    @Column(name = "termfrequency")
    private int termFrequency;

    public TermVectorEntity() {
    }

    public TermVectorId getId() {
        return id;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public TermVectorEntity setId(TermVectorId id) {
        this.id = id;
        return this;
    }

    public TermVectorEntity setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
        return this;
    }

}
