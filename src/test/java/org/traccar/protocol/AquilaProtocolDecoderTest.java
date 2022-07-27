package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolTest;

public class AquilaProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecodeA() throws Exception {

        var decoder = inject(new AquilaProtocolDecoder(null));

        verifyPosition(decoder, text(
                "$$CLIENT_1ZF,170215089,20,18.462809,73.824188,170613182744,A,01,123456,*37"));

        verifyPosition(decoder, text(
                "$$CLIENT_1ZF,170215089,1,18.462809,73.824188,170613182744,A,19,0,0,256,4,4.860000,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,259,3731,*37"));

        verifyPosition(decoder, text(
                "$$CLIENT_1ZF,170222318,101,22.846016,75.949104,170321103506,A,0,0,244991,0,10,0.860000,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,12483,294,*3D"));

        verifyNull(decoder, text(
                "$$CLIENT_1ZF,170222318,15,2_00AP,70.35.195.185,5089,internet,T1:10 S,T2:1 M,Ad1:9164061023,Ad2:9164061023,TOF:0 S,,OSC:75 KM,OST:0 S,GPS:YES,Ignition:ON,*75"));

        verifyPosition(decoder, text(
                "$$CLIENT_1ZF,170222318,1,22.836912,75.942215,170321110736,A,11,12,247196,103,10,0.810000,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,13325,306,*36"));

        verifyPosition(decoder, text(
                "$$CLIENT_1ZF,130329214,1,12.962985,77.576484,140127165433,A,22,0,0,140,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1253,420,1,0,1,0,0,1,P(01:410104000102|05:410521|0C:410C0000|0D:410D65|21:4121161C),D(P0121|B2105),-895,745,-145,300,*26"));

        verifyPosition(decoder, text(
                "$$CLIENT_1NS,101010119,1,22.845943,75.949059,170202184000,A,27,0,0,120,31141,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,399,3709,0,0,0,0,0,0,P(01:|04:|05:|0B:|0C:|0D:|10:|1C:|21:|23:|30:|31:|1F:|11:|00:|00:|),D(),-89,44,-1062,0,*49"));

        verifyPosition(decoder, text(
                "$$CLIENT_1DT,151028368,1,19.108438,72.925308,160628154920,A,22,0,0,131,3503,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,*1D"));

        verifyNull(decoder, text(
                "$$CLIENT_1DT,160319372,1,28.549541,77.249802,160628140743,A,23,0,-65025,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,*0D"));

        verifyPosition(decoder, text(
                "$$SRINI_1MS,141214807,1,12.963515,77.533844,150925161628,A,27,0,8,0,68,0,0,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,*43"));

        verifyPosition(decoder, text(
                "$$CLIENT_1ZF,130329214,1,12.962985,77.576484,140127165433,A,22,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,*26"));

        verifyPosition(decoder, text(
                "$$CLIENT_1WP,141216511,3,12.963123,77.534012,150908163534,A,31,0,0,0,7,0,0,0,0,0,0,0,1,0,1,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,*28"));

        verifyPosition(decoder, text(
                "$$CLIENT_1WP,141216511,3,12.963212,77.533989,150908164041,V,31,0,0,0,8,0,0,0,0,0,0,0,1,0,1,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,*2A"));

        verifyPosition(decoder, text(
                "$$CLIENT_1NS,866897057843486,1,13.144755,80.221474,220712112403,A,26,13,907034,263,15,0.830000,0,0,0,526336,13437,4004,3,21,1|010C:06410C14080000|010D:06410D03000000|0104:064104ED000000|010F:06410F6F000000|011F:06411F00050000|0121:06412100000000|0130:0641300D000000|0142:06414233CD0000|0131:06413103F30000|0143:067F43FFFF0000|015C:067F5CFF000000|012F:067F2FFF000000|0110:0641100C470000|0102:067F02FFFF0000|*5A"));
                
    }

}
