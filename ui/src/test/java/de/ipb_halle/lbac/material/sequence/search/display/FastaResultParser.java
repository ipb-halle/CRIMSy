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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResultBuilder;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResultBuilderException;
import de.ipb_halle.fasta_search_service.models.fastaresult.Frame;

/**
 * Parser implementation for the output of sequence searches by the fasta36
 * suite with the parameter "-m 10".
 * 
 * @author flange
 */
/*
 * This is a copy of
 * https://github.com/ipb-halle/fasta-search-service/blob/main/service/src/main/
 * java/de/ipb_halle/fasta_search_service/fastaresult/FastaResultParser.java
 */
public class FastaResultParser {
	private final BufferedReader reader;

	/**
	 * @param in sequence search output
	 */
	public FastaResultParser(Reader in) {
		reader = new BufferedReader(in);
	}

	/*
	 * Examples: "  1>>>query1 first query sequence - 50 aa", "  1>>> - 50 aa",
	 * "  1>>>query query sequence - 99 nt"
	 */
	private static final Pattern QUERY_START_PATTERN = Pattern.compile("[ \\d]+[>]{3}[\\w\\W]*[ ][-][ ][\\d]*[ ](aa|nt)");

	/*
	 * Examples: "; fa_frame: f", "; sw_frame: r"
	 */
	private static final Pattern FRAME_PATTERN = Pattern.compile("(;)[ ](fa_frame|sw_frame|fx_frame|fy_frame|tfx_frame|tfy_frame)(:).*");

	/*
	 * Examples: "; fa_expect: 5.2e-25", "; sw_expect:    1.7"
	 */
	private static final Pattern EVALUE_PATTERN = Pattern.compile("(;)[ ](fa_expect|sw_expect|fx_expect|fy_expect|tfx_expect|tfy_expect)(:).*");

	/*
	 * Examples: "; fa_bits: 96.5", "; sw_bits: 18.4"
	 */
	private static final Pattern BITSCORE_PATTERN = Pattern.compile("(;)[ ](fa_bits|sw_bits|fx_bits|fy_bits|tfx_bits|tfy_bits)(:).*");

	/*
	 * Example: "; sw_score: 313"
	 */
	private static final Pattern SWSCORE_PATTERN = Pattern.compile("(;)[ ](sw_score)(:).*");

	/*
	 * Examples: "; sw_ident: 1.000", "; bs_ident: 0.632"
	 */
	private static final Pattern IDENTITY_PATTERN = Pattern.compile("(;)[ ](sw_ident|bs_ident)(:).*");

	/*
	 * Examples: "; sw_sim: 1.000", "; bs_sim: 0.846"
	 */
	private static final Pattern SIMILARITY_PATTERN = Pattern.compile("(;)[ ](sw_sim|bs_sim)(:).*");

	/*
	 * Examples: "; sw_overlap: 50", "; bs_overlap: 13"
	 */
	private static final Pattern OVERLAP_PATTERN = Pattern.compile("(;)[ ](sw_overlap|bs_overlap)(:).*");

	/*
	 * Example: "; sq_len: 50"
	 */
	private static final Pattern SEQUENCE_LENGTH_PATTERN = Pattern.compile("(;)[ ](sq_len)(:).*");

	/*
	 * Example: "; al_start: 1"
	 */
	private static final Pattern ALIGNMENT_START_PATTERN = Pattern.compile("(;)[ ](al_start)(:).*");

	/*
	 * Example: "; al_stop: 50"
	 */
	private static final Pattern ALIGNMENT_STOP_PATTERN = Pattern.compile("(;)[ ](al_stop)(:).*");

	/*
	 * Example: "; al_display_start: 1"
	 */
	private static final Pattern ALIGNMENT_DISPLAY_START_PATTERN = Pattern.compile("(;)[ ](al_display_start)(:).*");

	/*
	 * matches "; al_cons:"
	 */
	private static final Pattern ALIGNMENT_CONSENSUS_PATTERN = Pattern.compile("(;)[ ](al_cons)(:)");

	/**
	 * Parse the sequence search output from the {@link Reader} object delivered in
	 * the constructor {@link #FastaResultParser(Reader)}.
	 * 
	 * @return list of results, not necessarily sorted by score
	 * @throws FastaResultParserException
	 */
	public List<FastaResult> parse() throws FastaResultParserException {
		try {
			return parseInput();
		} catch (IOException | FastaResultBuilderException e) {
			throw new FastaResultParserException(e);
		}
	}

	private List<FastaResult> parseInput() throws FastaResultParserException, IOException, FastaResultBuilderException {
		List<FastaResult> results = new ArrayList<>();
		String line;
		String querySequenceName = null;
		String querySequenceDescription = null;
		String subjectSequenceName = null;
		boolean parsedHeader = false;
		boolean parsedGlobalParameters = false;
		boolean inQueryBlock = false;
		boolean inSubjectBlock = false;
		boolean inSequence = false;
		StringBuilder sequenceBuilder = null;
		FastaResultBuilder builder = null;

		int lineNumber = 0;
		while ((line = reader.readLine()) != null) {
			lineNumber++;

			/*
			 * Query header: matches for example
			 * "  1>>>query1 first query sequence - 50 aa".
			 */
			if (!parsedHeader && QUERY_START_PATTERN.matcher(line).matches()) {
				// gives "query1 first query sequence - 50 aa"
				String rightPartOfQueryHeaderLine = StringUtils.substringAfter(line, ">>>");

				// gives "query1 first query sequence"
				String queryString = StringUtils.substringBeforeLast(rightPartOfQueryHeaderLine, "-").trim();

				int firstSpace = queryString.indexOf(" ");
				if (firstSpace < 0) {
					querySequenceName = queryString;
					querySequenceDescription = "";
				} else {
					querySequenceName = queryString.substring(0, firstSpace);
					querySequenceDescription = queryString.substring(firstSpace + 1);
				}
				parsedHeader = true;
			}

			/*
			 * Parameters header of this query: matches for example
			 * ">>>query1, 50 aa vs data.fasta library".
			 */
			else if (parsedHeader && !parsedGlobalParameters && line.startsWith(">>>" + querySequenceName)) {
				// nothing to parse here
				parsedGlobalParameters = true;
			}

			/*
			 * End of the result list for a query. This terminates the alignment consensus
			 * string and the result itself.
			 */
			else if (">>><<<".equals(line)) {
				if ((builder != null) && inSequence) {
					builder.consensusLine(sequenceBuilder.toString());
					inSequence = false;
				}

				// save the previous result
				if (builder != null) {
					results.add(builder.build());
					builder = null;
				}

				break;
			}

			/*
			 * Start of a result with a result header: matches for example
			 * ">>gb|AAF72530.1|AF252622_1 beta-lactamase CTX-M-14 (plasmid) [Escherichia coli]"
			 * . This also terminates the alignment consensus string of the previous result
			 * and the previous result itself.
			 */
			else if (parsedGlobalParameters && !inQueryBlock && line.startsWith(">>")) {
				if ((builder != null) && inSequence) {
					builder.consensusLine(sequenceBuilder.toString());
					inSequence = false;
				}

				// save the previous result
				if (builder != null) {
					results.add(builder.build());
					builder = null;
				}

				// begin new result
				builder = FastaResult.builder();
				inQueryBlock = false;
				inSubjectBlock = false;

				String subjectString = line.split(">>")[1];
				int firstSpace = subjectString.indexOf(" ");
				if (firstSpace < 0) {
					subjectSequenceName = subjectString;
					builder.subjectSequenceDescription("");
				} else {
					subjectSequenceName = subjectString.substring(0, firstSpace);
					builder.subjectSequenceDescription(subjectString.substring(firstSpace + 1));
				}
				builder.subjectSequenceName(subjectSequenceName);
			}

			/*
			 * Start of another hit result from the same subject sequence. The matching line
			 * is ">--". This also terminates the alignment consensus string of the previous
			 * result and the previous result itself.
			 */
			else if (parsedGlobalParameters && !inQueryBlock && inSequence && ">--".equals(line) && (builder != null)) {
				builder.consensusLine(sequenceBuilder.toString());
				inSequence = false;

				// save the previous result
				results.add(builder.build());
				String subjectSequenceDescription = builder.getSubjectSequenceDescription();

				// begin new result
				builder = FastaResult.builder();
				inQueryBlock = false;
				inSubjectBlock = false;

				builder.subjectSequenceName(subjectSequenceName);
				builder.subjectSequenceDescription(subjectSequenceDescription);
			}

			/*
			 * Frame
			 */
			else if ((builder != null) && FRAME_PATTERN.matcher(line).matches()) {
				builder.frame(Frame.fromPattern(parseStringAssignment(line)));
			}

			/*
			 * BitScore
			 */
			else if ((builder != null) && BITSCORE_PATTERN.matcher(line).matches()) {
				builder.bitScore(parseDoubleAssignment(line));
			}

			/*
			 * E()-value
			 */
			else if ((builder != null) && EVALUE_PATTERN.matcher(line).matches()) {
				builder.expectationValue(parseDoubleAssignment(line));
			}

			/*
			 * Smith-Waterman score
			 */
			else if ((builder != null) && SWSCORE_PATTERN.matcher(line).matches()) {
				builder.smithWatermanScore(parseIntAssignment(line));
			}

			/*
			 * Identity
			 */
			else if ((builder != null) && IDENTITY_PATTERN.matcher(line).matches()) {
				builder.identity(parseDoubleAssignment(line));
			}

			/*
			 * Similarity
			 */
			else if ((builder != null) && SIMILARITY_PATTERN.matcher(line).matches()) {
				builder.similarity(parseDoubleAssignment(line));
			}

			/*
			 * Overlap
			 */
			else if ((builder != null) && OVERLAP_PATTERN.matcher(line).matches()) {
				builder.overlap(parseIntAssignment(line));
			}

			/*
			 * Start of the query block: matches for example ">query1 ..".
			 */
			else if ((builder != null) && !inQueryBlock && !inSubjectBlock
					&& line.equals(">" + StringUtils.left(querySequenceName, 12) + " ..")) {
				inQueryBlock = true;
				builder.querySequenceName(querySequenceName).querySequenceDescription(querySequenceDescription);
			}

			/*
			 * Start of the subject block: matches for example
			 * ">gb|AAF72530.1|AF252622_1 ..". This also terminates the query alignment
			 * string and the query block.
			 */
			else if ((builder != null) && inQueryBlock && !inSubjectBlock
					&& line.equals(">" + subjectSequenceName + " ..")) {
				if (inSequence) {
					builder.queryAlignmentLine(sequenceBuilder.toString());
					inSequence = false;
					sequenceBuilder = null;
				}

				inQueryBlock = false;
				inSubjectBlock = true;
			}

			/*
			 * Sequence length
			 */
			else if ((builder != null) && SEQUENCE_LENGTH_PATTERN.matcher(line).matches()) {
				int value = parseIntAssignment(line);

				if (inQueryBlock && !inSubjectBlock) {
					builder.querySequenceLength(value);
				} else if (!inQueryBlock && inSubjectBlock) {
					builder.subjectSequenceLength(value);
				} else {
					throw new FastaResultParserException("Line " + lineNumber
							+ ": Sequence length (sq_len) detected outside of query or subject block.");
				}
			}

			/*
			 * Alignment start
			 */
			else if ((builder != null) && ALIGNMENT_START_PATTERN.matcher(line).matches()) {
				int value = parseIntAssignment(line);

				if (inQueryBlock && !inSubjectBlock) {
					builder.queryAlignmentStart(value);
				} else if (!inQueryBlock && inSubjectBlock) {
					builder.subjectAlignmentStart(value);
				} else {
					throw new FastaResultParserException("Line " + lineNumber
							+ ": Alignment start (al_start) detected outside of query or subject block.");
				}
			}

			/*
			 * Alignment stop
			 */
			else if ((builder != null) && ALIGNMENT_STOP_PATTERN.matcher(line).matches()) {
				int value = parseIntAssignment(line);

				if (inQueryBlock && !inSubjectBlock) {
					builder.queryAlignmentStop(value);
				} else if (!inQueryBlock && inSubjectBlock) {
					builder.subjectAlignmentStop(value);
				} else {
					throw new FastaResultParserException("Line " + lineNumber
							+ ": Alignment stop (al_stop) detected outside of query or subject block.");
				}
			}

			/*
			 * Alignment display start: The lines following this line make the alignment
			 * string.
			 */
			else if ((builder != null) && ALIGNMENT_DISPLAY_START_PATTERN.matcher(line).matches()) {
				int value = parseIntAssignment(line);

				if (inQueryBlock && !inSubjectBlock) {
					builder.queryAlignmentDisplayStart(value);
				} else if (!inQueryBlock && inSubjectBlock) {
					builder.subjectAlignmentDisplayStart(value);
				} else {
					throw new FastaResultParserException("Line " + lineNumber
							+ ": Alignment display start (al_display_start) detected outside of query or subject block.");
				}

				/*
				 * It is not possible to optimize the capacity of this StringBuilder, because
				 * the length of the alignment that is printed is not known (PS: One could
				 * certainly check the source code of the FASTA program to find this ...). Going
				 * for querySequenceLength or subjectSequenceLength could be overkill.
				 */
				sequenceBuilder = new StringBuilder();
				inSequence = true;
			}

			/*
			 * Start of alignment consensus: matches "; al_cons:". The lines following this
			 * line make the consensus string. This also terminates the subject alignment
			 * string and the subject block.
			 */
			else if ((builder != null) && ALIGNMENT_CONSENSUS_PATTERN.matcher(line).matches()) {
				if (inSequence) {
					builder.subjectAlignmentLine(sequenceBuilder.toString());
					inSequence = false;
					sequenceBuilder = null;
				}
				inSubjectBlock = false;

				// start the alignment string
				sequenceBuilder = new StringBuilder();
				inSequence = true;
			}

			/*
			 * Line of a sequence.
			 */
			else if ((builder != null) && inSequence) {
				sequenceBuilder.append(line);
			}
		}

		return results;
	}

	private String parseStringAssignment(String line) {
		return line.split(":")[1].trim();
	}

	private int parseIntAssignment(String line) {
		return Integer.parseInt(parseStringAssignment(line));
	}

	private double parseDoubleAssignment(String line) {
		String value = parseStringAssignment(line).replace("inf", "Infinity");
		return Double.parseDouble(value);
	}
}
