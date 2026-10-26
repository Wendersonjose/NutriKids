package br.com.nutrikids.planoalimentar;
import java.math.*;
import java.util.*;
/** Não estima necessidades individuais. Valores ausentes permanecem ausentes. */
public final class Composicao {
 private Composicao() {}
 public static Map<String,BigDecimal> porcao(Map<String,BigDecimal> por100g,BigDecimal gramas) {
  if(gramas==null||gramas.signum()<=0||por100g==null) throw new IllegalArgumentException("Quantidade inválida");
  var result=new TreeMap<String,BigDecimal>();
  por100g.forEach((k,v)->{if(v==null||v.signum()<0) throw new IllegalArgumentException("Nutriente inválido");result.put(k,v.multiply(gramas).divide(new BigDecimal("100"),6,RoundingMode.HALF_EVEN));});return result;
 }
 public static Map<String,BigDecimal> somar(List<Map<String,BigDecimal>> itens) {
  if(itens.isEmpty()) return Map.of();
  var keys=new HashSet<>(itens.get(0).keySet());itens.forEach(i->keys.retainAll(i.keySet()));
  var result=new TreeMap<String,BigDecimal>();
  for(String k:keys) result.put(k,itens.stream().map(i->i.get(k)).reduce(BigDecimal.ZERO,BigDecimal::add));
  return result;
 }

 /** Rendimento medido após preparo. Sem fatores de retenção ou perda presumidos. */
 public static Map<String,BigDecimal> preparacaoPor100g(List<Map<String,BigDecimal>> ingredientes,BigDecimal rendimentoGramas){
 if(rendimentoGramas==null||rendimentoGramas.signum()<=0)throw new IllegalArgumentException("Rendimento inválido");
 var result=new TreeMap<String,BigDecimal>();somar(ingredientes).forEach((k,v)->result.put(k,v.multiply(BigDecimal.valueOf(100)).divide(rendimentoGramas,6,RoundingMode.HALF_EVEN)));return result;
 }
}
