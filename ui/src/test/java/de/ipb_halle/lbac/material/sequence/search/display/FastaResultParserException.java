/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.display;

/**
 * @author flange
 */
/*
 * This is a copy of
 * https://github.com/ipb-halle/fasta-search-service/blob/main/service/src/main/
 * java/de/ipb_halle/fasta_search_service/fastaresult/FastaResultParserException
 * .java
 */
public class FastaResultParserException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public FastaResultParserException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public FastaResultParserException(Throwable cause) {
		super(cause);
	}
}