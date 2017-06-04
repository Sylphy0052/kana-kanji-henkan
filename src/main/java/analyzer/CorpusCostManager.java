package analyzer;

import analyzer.node.PartsOfSpeech;
import analyzer.node.ConnectionCost;
import javafx.util.Pair;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CorpusCostManager {
    private ArrayList<ConnectionCost> connectionCosts = new ArrayList<>();
    private Map<String, Integer> indexes = new LinkedHashMap<>();

    public CorpusCostManager(String csvFile) throws IOException {

        FileReader fileReader = new FileReader(ClassLoader.getSystemResource(csvFile).getFile());
        ICsvListReader reader = new CsvListReader(fileReader, CsvPreference.STANDARD_PREFERENCE);
        List<String> values;
        String before = "";

        //  input
        while ((values = reader.read()) != null) {
            try {
                String word = values.get(0);
                String kana = values.get(1);
                Double cost = Double.parseDouble(values.get(3));
                PartsOfSpeech partsOfSpeech = PartsOfSpeech.get(values.get(16));

                Double prenounAdjectival = Double.parseDouble(values.get(5));
                Double noun = Double.parseDouble(values.get(6));
                Double adverb = Double.parseDouble(values.get(7));
                Double verb = Double.parseDouble(values.get(8));
                Double prefix = Double.parseDouble(values.get(9));
                Double conjuction = Double.parseDouble(values.get(10));
                Double auxiliaryVerb = Double.parseDouble(values.get(11));
                Double particle = Double.parseDouble(values.get(12));
                Double adjective = Double.parseDouble(values.get(13));
                Double period = Double.parseDouble(values.get(14));
                Double interjection = Double.parseDouble(values.get(15));
                connectionCosts.add(new ConnectionCost(word, kana, partsOfSpeech, cost,
                        prenounAdjectival,
                        noun,
                        adverb,
                        verb,
                        prefix,
                        conjuction,
                        auxiliaryVerb,
                        particle,
                        adjective,
                        period,
                        interjection));
            } catch (RuntimeException e) {
                //e.printStackTrace();
                System.out.println("Ignored to parse the line of csv: " + values);
            }
        }

        //  indexing
        connectionCosts.sort((x1, x2) -> x1.getKana().compareTo(x2.getKana()));
        for (int i = 0; i < connectionCosts.size(); i++) {
            ConnectionCost connectionCost = connectionCosts.get(i);
            String initial = connectionCost.getKana();
            if (!initial.equals(before)) {
                before = initial;
                indexes.put(initial, i);
            }
        }
    }

    private Pair<Integer, Integer> getIndexRange(String targetWord) {
        Integer index = indexes.get(getFirstCharacter(targetWord));
        Integer first = null;
        for (Integer i = index; i < connectionCosts.size(); i++) {
            ConnectionCost connectionCost = connectionCosts.get(i);
            if (first == null) {
                if (targetWord.equals(connectionCost.getKana())) {
                    first = i;
                } else if (connectionCost.getKana().startsWith(targetWord)) {
                    return new Pair<>(i - 1, i - 1);
                }
            } else if (!targetWord.equals(connectionCost.getKana())) {
                return new Pair<>(first, i);
            }
        }
        return new Pair<>(-1, -1);
    }

    public List<ConnectionCost> findAllByKana(String word) {
        Pair<Integer, Integer> interval = getIndexRange(word);
        if (interval.getKey() > 0) {
            return connectionCosts.subList(interval.getKey(), interval.getValue());
        }
        return Collections.emptyList();
    }

    public Boolean isExistPartialMatchWord(String word) {
        Pair<Integer, Integer> interval = getIndexRange(word);
        if (interval.getKey() > 0 && interval.getValue() + 1 < connectionCosts.size()) {
            ConnectionCost connectionCost = connectionCosts.get(interval.getValue() + 1);
            return connectionCost.getKana().startsWith(word);
        }
        return false;
    }

    public static String getFirstCharacter(String text) {
        List<String> strings = sparateCharacters(text);
        return strings.size() > 0 ? strings.get(0) : "";
    }

    public static List<String> sparateCharacters(String text) {
        return Arrays
                .stream(text.split(""))
                .filter(x -> !Objects.equals(x, ""))
                .collect(Collectors.toList());
    }
}
