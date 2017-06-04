package analyzer;

import analyzer.node.PartsOfSpeech;
import analyzer.node.ConnectionCost;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Value
public class SyntaxAnalyzer {
    private final CorpusCostManager corpusCostManager;

    public SyntaxAnalysisResult convert(String text) {

        ArrayList<ConnectionCost> words = new ArrayList<>();
        Integer currentWordStringPoint = 0;
        List<String> strings = CorpusCostManager.sparateCharacters(text);

        while (currentWordStringPoint < strings.size()) {
            Integer currentStringPoint = 0;
            StringBuilder stringBuilder = new StringBuilder();
            Multimap<Double, ConnectionCost> costTreeMap = TreeMultimap.create(Double::compare, (x1, x2) -> x1.getKana().compareTo(x2.getKana()));

            while (currentWordStringPoint + currentStringPoint < strings.size()) {
                stringBuilder.append(strings.get(currentWordStringPoint + currentStringPoint));
                String currentWord = stringBuilder.toString();

                List<ConnectionCost> list = corpusCostManager.findAllByKana(currentWord);
                list.forEach(x -> costTreeMap.put(calculateCost(words, x), x));

                if (list.size() == 0 && !corpusCostManager.isExistPartialMatchWord(currentWord)) {
                    break;
                }
                currentStringPoint++;
            }

            List<ConnectionCost> list = costTreeMap.values().stream().collect(Collectors.toList());
            if (list.size() == 0) {
                continue;
            }

            System.out.println(list);
            words.add(list.get(0));
            currentWordStringPoint += list.get(0).getKana().length();
        }

        return new SyntaxAnalysisResult(words);
    }

    private Double calculateCost(final List<ConnectionCost> wordHistories, ConnectionCost connectionCost) {
        PartsOfSpeech partsOfSpeech;
        if (wordHistories.size() == 0) {
            partsOfSpeech = PartsOfSpeech.PERIOD;
        } else {
            ConnectionCost previous = wordHistories.get(wordHistories.size() - 1);
            partsOfSpeech = previous.getPartsOfSpeech();
        }

        Double addition = 1.0 - connectionCost.getKana().length() * 0.1;
        Double hCost = 9.0;

        switch (partsOfSpeech) {
            case NOUN:
                hCost = connectionCost.getNoun();
                break;
            case ADJECTIVE:
                hCost = connectionCost.getAdjective();
                break;
            case VERB:
                hCost = connectionCost.getVerb();
                break;
            case ADVERB:
                hCost =  connectionCost.getAdverb();
                break;
            case CONJUCTION:
                hCost = connectionCost.getConjuction();
                break;
            case INTERJECTION:
                hCost = connectionCost.getInterjection();
                break;
            case PARTICLE:
                hCost = connectionCost.getParticle();
                break;
            case PRENOUN_ADJECTIVAL:
                hCost = connectionCost.getAdjective();
                break;
            case AUXILIARY_VERB:
                hCost = connectionCost.getAuxiliaryVerb();
                break;
            case PREFIX:
                hCost = connectionCost.getPrefix();
                break;
            case PERIOD:
                hCost = connectionCost.getPeriod();
                break;
            default:
                break;
        }
        return addition * hCost + connectionCost.getCost();
    }
}
