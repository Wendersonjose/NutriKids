package br.com.nutrikids.auth;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class LimiteApiTest {@Test void limitaIsolaERenova(){var l=new LimiteApi();for(int n=0;n<240;n++)assertTrue(l.permitir("a",0));assertFalse(l.permitir("a",59999));assertTrue(l.permitir("b",59999));assertTrue(l.permitir("a",60000));}}
