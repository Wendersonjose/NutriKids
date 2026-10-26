package br.com.nutrikids.antropometria;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.stereotype.Service;

/** OMS 2006, dados diários oficiais. Sem diagnóstico ou extrapolação etária. */
@Service
public class OmsService {
  record Lms(double l,double m,double s) {}
  public record Resultado(String indicador,Double z,Double percentil,boolean inconsistente,String aviso) {}
  private final Map<String,Map<String,Lms>> tabelas = new HashMap<>();
  public OmsService() {
    for(String nome:List.of("wei","len","bmi","wfl","wfh")) {
      Map<String,Lms> linhas=new HashMap<>();
      try(var stream=Objects.requireNonNull(getClass().getResourceAsStream("/oms/"+nome+"anthro.txt"));
          var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
        reader.readLine(); String line;
        while((line=reader.readLine())!=null) {
          var a=line.trim().split("\\s+");
          linhas.put(a[0]+":"+Double.parseDouble(a[1]),new Lms(Double.parseDouble(a[2]),Double.parseDouble(a[3]),Double.parseDouble(a[4])));
        }
      } catch(IOException e) {throw new IllegalStateException("Tabela OMS indisponível",e);}
      tabelas.put(nome,linhas);
    }
  }
  static double valor(Lms p,double z) {return p.l==0?p.m*Math.exp(p.s*z):p.m*Math.pow(1+p.l*p.s*z,1/p.l);}
  static double escore(Lms p,double y,boolean restrito) {
    double z=p.l==0?Math.log(y/p.m)/p.s:(Math.pow(y/p.m,p.l)-1)/(p.l*p.s);
    if(restrito && z>3) return 3+(y-valor(p,3))/(valor(p,3)-valor(p,2));
    if(restrito && z< -3) return -3+(y-valor(p,-3))/(valor(p,-2)-valor(p,-3));
    return z;
  }
  static double percentil(double z) {
    // Aproximação normal de Abramowitz-Stegun 26.2.17, erro absoluto < 7.5e-8.
    double t=1/(1+0.2316419*Math.abs(z));
    double q=Math.exp(-z*z/2)/Math.sqrt(2*Math.PI)*t*(0.319381530+t*(-0.356563782+t*(1.781477937+t*(-1.821255978+t*1.330274429))));
    return 100*(z>=0?1-q:q);
  }
  public Resultado calcular(String indicador,String sexo,long dias,double peso,double altura,String posicao,boolean edema) {
    if(!List.of("wei","len","bmi","wfl").contains(indicador)) throw new IllegalArgumentException("Indicador inválido");
    if(!Double.isFinite(peso)||!Double.isFinite(altura)||peso<=0||altura<=0) throw new IllegalArgumentException("Medidas devem ser positivas e finitas");
    if(!List.of("DEITADO","EM_PE").contains(posicao)) throw new IllegalArgumentException("Posição inválida");
    if(!List.of("F","M").contains(sexo)) return indisponivel(indicador,"Sexo de referência não informado");
    if(dias<0||dias/30.4375>=60) return indisponivel(indicador,"Fora da faixa implementada: 0 a menos de 60 meses; referência 2007 pendente");
    if(edema && !indicador.equals("len")) return indisponivel(indicador,"Edema: indicador baseado em peso não aplicável");
    double h=altura+(dias<731&&posicao.equals("EM_PE")?0.7:dias>=731&&posicao.equals("DEITADO")?-0.7:0);
    String tabela=indicador.equals("wfl")?(dias<731?"wfl":"wfh"):indicador;
    double chave=dias;
    Lms p;
    String s=sexo.equals("M")?"1:":"2:";
    if(indicador.equals("wfl")) {
      if(h<(dias<731?45:65)||h>(dias<731?110:120)) return indisponivel(indicador,"Comprimento/altura fora da tabela OMS");
      // Interpolação entre décimos conforme rotina oficial anthro de peso por comprimento/altura.
      double lo=Math.floor(h*10+1e-8)/10, hi=Math.ceil(h*10-1e-8)/10;
      Lms a=tabelas.get(tabela).get(s+lo), b=tabelas.get(tabela).get(s+hi);
      if(a==null||b==null) return indisponivel(indicador,"Linha OMS ausente");
      double f=(h-lo)*10;
      p=new Lms(a.l+f*(b.l-a.l),a.m+f*(b.m-a.m),a.s+f*(b.s-a.s));
    } else p=tabelas.get(tabela).get(s+chave);
    if(p==null) return indisponivel(indicador,"Linha OMS ausente");
    double y=indicador.equals("len")?h:indicador.equals("bmi")?peso/Math.pow(h/100,2):peso;
    double z=escore(p,y,!indicador.equals("len"));
    boolean flag=switch(indicador) {case "wei" -> z< -6||z>5;case "len" -> Math.abs(z)>6;default -> Math.abs(z)>5;};
    return new Resultado(indicador,Math.rint(z*100)/100,percentil(z),flag,flag?"Revisar medição; não representa diagnóstico":"Revisão profissional necessária");
  }
  private Resultado indisponivel(String i,String motivo) {return new Resultado(i,null,null,false,motivo);}
}
