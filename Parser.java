import java.util.ArrayList;
import java.util.List;

import computation.contextfreegrammar.*;
import computation.parser.*;
import computation.parsetree.*;
import computation.derivation.*;

public class Parser implements IParser {

  //

  List<Rule> getSymbolRules(Symbol v, ContextFreeGrammar cfg) {
    List<Rule> symbolRules = new ArrayList<>();

    if (v.isTerminal()) {
        return symbolRules; // Return an empty list since terminals don't have rules
    }

    List<Rule> rules = cfg.getRules();
    for (Rule r : rules) {
        Symbol variable = r.getVariable();
        if (variable.equals(v)) {
            symbolRules.add(r);
        }
    }

    return symbolRules;
}

private List<Derivation> generateDerivations(ContextFreeGrammar cfg, int n) {
  List<Derivation> allDerivations = new ArrayList<>();

  Variable startVariable = cfg.getStartVariable();
  Derivation zerothDerivation = new Derivation(new Word(startVariable));
  allDerivations.add(zerothDerivation);

  for (int i = 0; i < n; i++) {
      List<Derivation> derivationsToAdd = new ArrayList<>();

      for (Derivation derivation : allDerivations) {
          Word word = derivation.getLatestWord();

          boolean added = false;
          int index = 0;
          for (Symbol symbol : word) {
              if (symbol.isTerminal()) {
                  index++;
                  continue;
              }

              List<Rule> rules = getSymbolRules(symbol, cfg);

              for (Rule rule : rules) {
                  Word expansion = rule.getExpansion();
                  Word newWord = word.replace(index, expansion);

                  if (added) {
                      Derivation newDerivation = new Derivation(derivation);
                      newDerivation.addStep(newWord, rule, index);
                      derivationsToAdd.add(newDerivation);
                  } else {
                      Derivation newDerivation = new Derivation(derivation);
                      newDerivation.addStep(newWord, rule, index);
                      derivationsToAdd.add(newDerivation);
                      added = true;
                  }
              }
              index++;
          }
      }

      allDerivations.addAll(derivationsToAdd);
  }

  return allDerivations;
}


public boolean isInLanguage(ContextFreeGrammar cfg, Word w) {
  int wordLength = w.length();
  int numberDerivations;
  if (wordLength == 0) {
    numberDerivations = 1;
  } else {
    numberDerivations = (2 * wordLength) - 1;
  }

  List<Derivation> allDerivations = generateDerivations(cfg, numberDerivations);

  for (Derivation derivation: allDerivations) {
    if (w.equals(derivation.getLatestWord())) {
      return true;
    }
  }
  return false;
}

/**
 * Builds a parse tree from a derivation.
 * @param derivation The derivation to build the parse tree from.
 * @return The root node of the parse tree.
 */

private ParseTreeNode buildParseTree(Derivation derivation) {
  Word finalWord = derivation.getLatestWord();
  List<ParseTreeNode> leafNodes = new ArrayList<>();

  // Create leaf nodes for each symbol in the final word
  for (Symbol symbol : finalWord) {
      leafNodes.add(new ParseTreeNode(symbol));
  }

  // Build the parse tree by traversing the derivation
  for (Step step : derivation) {
      Rule parentRule = step.getRule();
      if (parentRule == null) {
          break;
      }

      Symbol parentSymbol = parentRule.getVariable();
      int stepIndex = step.getIndex();
      Word expansion = parentRule.getExpansion();

      // Determine whether to create a parent node with one or two child nodes
      if (expansion.length() > 1) {
          ParseTreeNode parentNode = new ParseTreeNode(parentSymbol, leafNodes.get(stepIndex), leafNodes.get(stepIndex + 1));
          leafNodes.remove(stepIndex);
          leafNodes.remove(stepIndex);
          leafNodes.add(stepIndex, parentNode);
      } else {
          ParseTreeNode parentNode = new ParseTreeNode(parentSymbol, leafNodes.get(stepIndex));
          leafNodes.remove(stepIndex);
          leafNodes.add(stepIndex, parentNode);
      }
  }

  return leafNodes.get(0); // Return the root node of the parse tree
}

/**
 * Generates a parse tree for a given word in the context-free grammar.
 * @param cfg The context-free grammar.
 * @param w The word to generate the parse tree for.
 * @return The parse tree for the given word, or null if the word is not in the language.
 */
public ParseTreeNode generateParseTree(ContextFreeGrammar cfg, Word w) {
    if (isInLanguage(cfg, w)) {
      int wordLength = w.length();
      if (wordLength == 0) {
        ParseTreeNode ptn = ParseTreeNode.emptyParseTree(cfg.getStartVariable());
        return ptn;
      }

      int numberDerivations = (2 * wordLength) - 1;
      List<Derivation> allDerivations = generateDerivations(cfg, numberDerivations);
      for (Derivation derivation: allDerivations) {
        if (w.equals(derivation.getLatestWord())) {
          ParseTreeNode ptn = buildParseTree(derivation);
          return ptn;
        }
      }
    }
    return null;
  }

}