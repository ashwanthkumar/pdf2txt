package in.ashwanthkumar.pdf2txt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.Bidi;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Normalizer {
    private static final Log LOG = LogFactory.getLog(Normalizer.class);

    /**
     * Copied verbatim from org.apache.pdfbox.text.PDFTextStripper class
     * <p>
     * Normalize certain Unicode characters. For example, convert the single "fi" ligature to "f" and "i". Also
     * normalises Arabic and Hebrew presentation forms.
     *
     * @param word Word to normalize
     * @return Normalized word
     */
    public static String normalizeWord(String word) {
        StringBuilder builder = null;
        int p = 0;
        int q = 0;
        int strLength = word.length();
        for (; q < strLength; q++) {
            // We only normalize if the codepoint is in a given range.
            // Otherwise, NFKC converts too many things that would cause
            // confusion. For example, it converts the micro symbol in
            // extended Latin to the value in the Greek script. We normalize
            // the Unicode Alphabetic and Arabic A&B Presentation forms.
            char c = word.charAt(q);
            if (0xFB00 <= c && c <= 0xFDFF || 0xFE70 <= c && c <= 0xFEFF) {
                if (builder == null) {
                    builder = new StringBuilder(strLength * 2);
                }
                builder.append(word.substring(p, q));
                // Some fonts map U+FDF2 differently than the Unicode spec.
                // They add an extra U+0627 character to compensate.
                // This removes the extra character for those fonts.
                if (c == 0xFDF2 && q > 0
                        && (word.charAt(q - 1) == 0x0627 || word.charAt(q - 1) == 0xFE8D)) {
                    builder.append("\u0644\u0644\u0647");
                } else {
                    // Trim because some decompositions have an extra space, such as U+FC5E
                    builder.append(java.text.Normalizer
                            .normalize(word.substring(q, q + 1), java.text.Normalizer.Form.NFKC).trim());
                }
                p = q + 1;
            }
        }
        if (builder == null) {
            return handleDirection(word);
        } else {
            builder.append(word.substring(p, q));
            return handleDirection(builder.toString());
        }
    }

    private static Map<Character, Character> MIRRORING_CHAR_MAP = new HashMap<Character, Character>();

    static {
        String path = "/org/apache/pdfbox/resources/text/BidiMirroring.txt";
        InputStream input = PDFTextStripper.class.getResourceAsStream(path);
        try {
            if (input != null) {
                parseBidiFile(input);
            } else {
                LOG.warn("Could not find '" + path + "', mirroring char map will be empty: ");
            }
        } catch (IOException e) {
            LOG.warn("Could not parse BidiMirroring.txt, mirroring char map will be empty: "
                    + e.getMessage());
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                LOG.error("Could not close BidiMirroring.txt ", e);
            }
        }
    }

    /**
     * This method parses the bidi file provided as inputstream.
     *
     * @param inputStream - The bidi file as inputstream
     * @throws IOException if any line could not be read by the LineNumberReader
     */
    private static void parseBidiFile(InputStream inputStream) throws IOException {
        LineNumberReader rd = new LineNumberReader(new InputStreamReader(inputStream));

        do {
            String s = rd.readLine();
            if (s == null) {
                break;
            }

            int comment = s.indexOf('#'); // ignore comments
            if (comment != -1) {
                s = s.substring(0, comment);
            }

            if (s.length() < 2) {
                continue;
            }

            StringTokenizer st = new StringTokenizer(s, ";");
            int nFields = st.countTokens();
            Character[] fields = new Character[nFields];
            for (int i = 0; i < nFields; i++) {
                fields[i] = (char) Integer.parseInt(st.nextToken().trim(), 16);
            }

            if (fields.length == 2) {
                // initialize the MIRRORING_CHAR_MAP
                MIRRORING_CHAR_MAP.put(fields[0], fields[1]);
            }

        } while (true);
    }

    /**
     * Handles the LTR and RTL direction of the given words. The whole implementation stands and falls with the given
     * word. If the word is a full line, the results will be the best. If the word contains of single words or
     * characters, the order of the characters in a word or words in a line may wrong, due to RTL and LTR marks and
     * characters!
     * <p>
     * Based on http://www.nesterovsky-bros.com/weblog/2013/07/28/VisualToLogicalConversionInJava.aspx
     *
     * @param word The word that shall be processed
     * @return new word with the correct direction of the containing characters
     */
    private static String handleDirection(String word) {
        Bidi bidi = new Bidi(word, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);

        // if there is pure LTR text no need to process further
        if (!bidi.isMixed() && bidi.getBaseLevel() == Bidi.DIRECTION_LEFT_TO_RIGHT) {
            return word;
        }

        // collect individual bidi information
        int runCount = bidi.getRunCount();
        byte[] levels = new byte[runCount];
        Integer[] runs = new Integer[runCount];

        for (int i = 0; i < runCount; i++) {
            levels[i] = (byte) bidi.getRunLevel(i);
            runs[i] = i;
        }

        // reorder individual parts based on their levels
        Bidi.reorderVisually(levels, 0, runs, 0, runCount);

        // collect the parts based on the direction within the run
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < runCount; i++) {
            int index = runs[i];
            int start = bidi.getRunStart(index);
            int end = bidi.getRunLimit(index);

            int level = levels[index];

            if ((level & 1) != 0) {
                while (--end >= start) {
                    char character = word.charAt(end);
                    if (Character.isMirrored(word.codePointAt(end))) {
                        if (MIRRORING_CHAR_MAP.containsKey(character)) {
                            result.append(MIRRORING_CHAR_MAP.get(character));
                        } else {
                            result.append(character);
                        }
                    } else {
                        result.append(character);
                    }
                }
            } else {
                result.append(word, start, end);
            }
        }

        return result.toString();
    }
}
