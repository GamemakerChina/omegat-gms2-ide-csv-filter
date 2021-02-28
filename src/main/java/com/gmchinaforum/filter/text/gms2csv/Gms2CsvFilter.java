package com.gmchinaforum.filter.text.gms2csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter to support Files for GameMaker Studio 2 Language locale.
 * "Name","English","Translation","Restrictions","Comment"
 *
 * @author GameMaker China Forum
 */
public class Gms2CsvFilter extends AbstractFilter {
    protected Map<String, String> align;

    public static void loadPlugins() {
        Core.registerFilterClass(Gms2CsvFilter.class);
    }

    public static void unloadPlugins() {
    }

    public String getFileFormatName() {
        return "GameMaker Studio 2 Language CSV";
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return false;
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.csv", null, "UTF-8"), };
    }

    /**
     * Doing the processing of the file...
     * @param reader
     * @param outfile
     */
    @Override
    public void processFile(BufferedReader reader, BufferedWriter outfile, FilterContext fc) throws IOException {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader); // fix
                                                                                // for
                                                                                // bug
                                                                                // 1462566
        String line;
        Pattern splitter = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        while ((line = lbpr.readLine()) != null) {
            String trimmed = line.trim();

            // skipping empty strings
            if (trimmed.isEmpty()) {
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }

            String[] result = splitter.split(trimmed);

            String name = result[0];
            String english = result[1];
            String translation = result[2];

            // Fix ArrayIndexOutOfBounds exception 
            // If you use general usage, it will cause the array to go out of bounds, strange problems:
            // String restrictions = result[3];
            // String comments = result[4];
            String restrictions = null;
            String comments = null;
            for(int j = 3; j < result.length - 1; j++) {
                restrictions = result[j];
            }
            for(int j = 4; j < result.length; j++) {
                comments = result[j];
            }

            // writing out: name,english,translation,restrictions,comments

            String trans = process(name, translation, comments);

            outfile.write(name + "," + english + "," + trans + "," + restrictions + "," + comments);
            //outfile.write(name + "," + english + "," + trans);
            outfile.write(lbpr.getLinebreak());
        }
        lbpr.close();
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile,
            org.omegat.filters2.FilterContext fc) throws Exception {
        Map<String, String> source = new HashMap<String, String>();
        Map<String, String> translated = new HashMap<String, String>();

        align = source;
        processFile(sourceFile, new NullBufferedWriter(), fc);
        align = translated;
        processFile(translatedFile, new NullBufferedWriter(), fc);
        for (Map.Entry<String, String> en : source.entrySet()) {
            String tr = translated.get(en.getKey());
            if (!StringUtil.isEmpty(tr)) {
                entryAlignCallback.addTranslation(en.getKey(), en.getValue(), tr, false, null, this);
            }
        }
    }

    /**
     * @param name
     * @param translation
     * @param comments
     * @return
     */
    private String process(String name, String translation, String comments) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(name, translation, null, false, comments, this);
            return translation;
        } else if (entryTranslateCallback != null) {
            String trans = entryTranslateCallback.getTranslation(name, translation);
            return trans != null ? trans : translation;
        } else if (entryAlignCallback != null) {
            align.put(name, translation);
        }
        return translation;
    }

}
