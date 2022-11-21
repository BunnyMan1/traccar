package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolTest;
import org.traccar.model.Position;

public class Gt06ProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        var decoder = inject(new Gt06ProtocolDecoder(null));

        verifyNull(decoder, binary(
                "787805120099abec0d0a"));

        verifyNull(decoder, binary(
                "78780D01086471700328358100093F040D0A"));

        verifyAttribute(decoder, binary(
                "78782526160913063918c002780fab0c44750f00040008027f14084c0038420600030c020007398e0d0a"),
                Position.KEY_ALARM, Position.ALARM_TAMPERING);

        verifyAttribute(decoder, binary(
                "7979000d940516090908081c030defbd2d0d0a"),
                Position.KEY_DOOR, true);

        verifyAttribute(decoder, binary(
                "78782516160908063736c0006e70110442fc800000000800000000000000000300002512000473fb0d0a"),
                Position.KEY_ALARM, Position.ALARM_TAMPERING);

        verifyPosition(decoder, binary(
                "78782e2416061a103600c80275298404a0a24000184602d4023a49006f060104ed01940000086508004139765000be7d640d0a"));

        verifyAttribute(decoder, binary(
                "79790019941b524649443a3030384642324245424133390d0a000c14930d0a"),
                "serial", "RFID:008FB2BEBA39");

        verifyAttribute(decoder, binary(
                "7878241216040e102c22cf00915ffb04c6016300195a02d402283b00753f400571040001dda4880d0a"),
                Position.KEY_IGNITION, false);

        verifyNotNull(decoder, binary(
                "787831241603060c231e000194620213ee00606a3413ee0060692e000000000000000000000000000000000000000000003a0cb70d0a"));

        verifyNotNull(decoder, binary(
                "78783B2810010D02020201CC00287D001F713E287D001F7231287D001E232D287D001F4018000000000000000000000000000000000000FF00020005B14B0D0A"));

        verifyNotNull(decoder, binary(
                "78782111150b0b022c30c804b7af7808810cb0003c00012e02d075df0084890c000679950d0a"));

        verifyNotNull(decoder, binary(
                "797900377000000001020035000103002c0004616219d00043000b013601048153931500001a0001000808652820400643521000000101004e46760d0a"));

        verifyNull(decoder, binary(
                "7878171915061810051a01f90101700d08c8f50c0000065494ae0d0a"));

        verifyNotNull(decoder, binary(
                "78783B2E10010D02020201CC00287D001F713E287D001F7231287D001E232D287D001F4018000000000000000000000000000000000000FF00020005B14B0D0A"));

        verifyPosition(decoder, binary(
                "787822220F0C1D023305C9027AC8180C46586000140001CC00287D001F71000001000820860D0A"));

        verifyAttribute(decoder, binary(
                "78782b1215050d03041bcf031ff30a0be795bc001c17014e14a065dd95314504b6040000001c00000cd90ab8fb6f0d0a"),
                Position.PREFIX_TEMP + 1, 0x1c);

        verifyAttribute(decoder, binary(
                "7878151330802b00000642014f0008720000802b5ee4d4c90d0a"),
                Position.KEY_BATTERY_LEVEL, 6);

        verifyAttribute(decoder, binary(
                "7878281520000000003c49434349443a38393838323339303030303039373330323635303e00020d446f260d0a"),
                Position.KEY_ICCID, "89882390000097302650");

        verifyNull(decoder, binary(
                "797900099b0380d600046f91e90d0a"));

        verifyNull(decoder, binary(
                "797900a56615010d081f3b012c323131303d30303033643238342c323130353d30303030316332302c323130623d30303030326537632c323130633d30303033643238342c323130663d30303030306331632c323130643d30303030323166632c323161363d30303030303030302c323130343d30303030306531302c323132663d30303030303030302c323134353d30303030303030302ccb03851f5f03c020525514a7003e216a0d0a"));

        verifyNotNull(decoder, binary(
                "797900cd8c15010d08200d013137333d302c3232333d312c3238333d30303032373464382c3436333d30303030303239352c3437333d30303030363333622c3438333d30303030303036372c3242333d30303030303030302c3244333d30303030316332302c3335333d30303030323133342c3336333d30303033626430382c3339333d30303030303336622c3330333d30303030303539362c3439333d30303030306563382c3441333d2c3341333d30303030303430352c3530333d30303030cb03850c3603c025af5414a6003fc5940d0a"));

        verifyNotNull(decoder, binary(
                "787844F3140C0B0A262A070000DC9FDB1C1D760000C83A3569A37100008825937287000000EC41180C8209000088C39749553700003891D5604D6300003891D5604D68002EC4230D0A"));

        verifyAttribute(decoder, binary(
                "7979000E9B0332382E33A1E60D0A0289BE490D0A"),
                Position.PREFIX_TEMP + 1, 28.3);

        verifyPosition(decoder, binary(
                "7878353714080d05000ac500a886eb0b7522f000100001fe0a05ea004f1b000001002e0400002328003b0217c0003c0401020001002c468a0d0a"));

        verifyAttribute(decoder, binary(
                "79790020940a035985708236675805200502187214018966051912408052452f000355560d0a"),
                Position.KEY_ICCID, "8966051912408052452");

        verifyPosition(decoder, binary(
                "7878252612030C063816C3026C10540C38C9700144030901CC002866000EEE0C06040302000DA2DB0D0A"));

        verifyAttribute(decoder, binary(
                "78784A1614051C150204EC02777E560C2A2A2A5314AF000000000000040000000000000000000000000000000000000000000000000000000000000000000000000000007E061B0982004B24C00D0A"),
                Position.KEY_BATTERY_LEVEL, 100);

        verifyAttribute(decoder, binary(
                "7979000e941b02084277efef350303eadaed0d0a"),
                Position.KEY_DRIVER_UNIQUE_ID, "4277efef");

        verifyAttributes(decoder, binary(
                "7878711213081f081d0fc6017ba3fa0ac62a923e550e02080503f300b26d000000004b20202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202030300017c7470d0a"));

        verifyAttributes(decoder, binary(
                "797900B2700000000102003500010400330012000000000000000000000000000000000000003400061354A48DFF00003400061154A48E56000011000A000000000000000000000001000803537601000282180002000802140743044211890003000A89340752000038689636001800020182002B000116002C000454A4FF350009000100000A0001010028000100002E000400000000002A00010000290004000000000030000A000101680014016802D00000B38F0D0A"));

        verifyAttribute(decoder, binary(
                "78780c95130a071223200100013ad10d0a"),
                Position.KEY_ALARM, Position.ALARM_GENERAL);

        verifyAttribute(decoder, binary(
                "78783c3400000000130a1906011105029b4d450b1828d5001c00000000009e7d014e140fc000004e990000000004c301a442210000020101c001c0000591aa0d"),
                Position.PREFIX_ADC + 1, 4.48);

        verifyAttribute(decoder, binary(
                "797900a87000000001020035000101003300125d7e3a180600d504b598f708814b3a001d1500340006125d7e39dc000011000a012e02620000000000000001000803537601000129800002000803102608593397620003000a89012608522933976266001800020172002b000114002c00045d7df3c70009000106000a000109002800010d002e00040000f25d002a000111002900040000017e0030000a000100b4000a00b402d0000591250d0a"),
                Position.KEY_ALARM, Position.ALARM_REMOVING);

        verifyPosition(decoder, binary(
                "797900a87000000001020035000100003300125d62bf3a0800e804b5994308814a87001d5d00340006115d62bf29000011000a012e02620000000000000001000803537601000129800002000803102608593397620003000a8901260852293397626600180002017d002b000116002c00045d6278ea0009000108000a00010b002800010b002e00040000f0c1002a00010000290004000000be0030000a000100b4000a00b402d00006c5490d0a"));

        verifyAttribute(decoder, binary(
                "797900149b03023539303042343843454238410300139ba40d0a"),
                Position.KEY_DRIVER_UNIQUE_ID, "5900B48CEB");

        verifyPosition(decoder, binary(
                "787821121303120b2524c70138e363085b549003d43301940057d200cd52c000006aa1ca0d0a"));

        verifyAttribute(decoder, binary(
                "7878251613020C12141AC5027951430C2A16F60014000900000000001A00007C550300020002F1E70D0A"),
                Position.KEY_ALARM, Position.ALARM_REMOVING);

        verifyNotNull(decoder, binary(
                "7878006919012105090303028f01007549e05a00bc9c5c5a007a8d1a5a0d0a"));

        verifyNotNull(decoder, binary(
                "7878001719011910543607028f0100bc2e695a00bcb3635a00bc27c56400bc447b6400bc46c96400bc33ce6400bc64ca640d0a"));

        verifyAttributes(decoder, binary(
                "797900de8c120b1502121b013137333d302c3232333d312c3238333d30303030303030302c3436333d30303030303035362c3437333d30303030303030662c3438333d30303030303031312c3242333d30303030303030302c3244333d30303030313839632c3335333d30303030313661382c3336333d30303032386163382c3339333d30303030303230612c3330333d30303030303561612c3439333d30303030303030302c3441333d4b4e4146323431434d4b353031343235352c3341333d30303030303338352c3530333d30303030c0041df0940f89c06700000000c800910d0a"));

        verifyAttributes(decoder, binary(
                "79790020940A035985708053908307060104900402788950301217070401538F0003E8210D0A"));

        verifyPosition(decoder, binary(
                "78783c3439000000120a0902093a07031f9e690529be2e00155500000016214901a30308b70000b3fb004aa82b059401a3422100000001000000007d9370b90d0a"));

        verifyAttributes(decoder, binary(
                "7878079404eb001c705d0d0a"));

        verifyNotNull(decoder, binary(
                "78781219028f0a29e10036f12003040102000875dc0d0a"));

        verifyAttributes(decoder, binary(
                "79790008940000ed0289d6860d0a"));

        verifyNull(decoder, binary(
                "797900a59404414c4d313d34353b414c4d323d44353b414c4d333d35353b535441313d34303b4459443d30313b534f533d303538353036313536372c2c3b43454e5445523d3b46454e43453d46656e63652c4f46462c302c302e3030303030302c302e3030303030302c3330302c494e206f72204f55542c303b49434349443d38393937313033313031303038393539303432463b4d4f44453d4d4f44452c312c3138303b0008f65e0d0a"));

        verifyPosition(decoder, binary(
                "78782222120616083817c5050cc8c801a819d600152400e8011dbf003332000000004862500d0a"));

        verifyAttributes(decoder, binary(
                "78780B23C00122040001000818720D0A"));

        verifyNotNull(decoder, binary(
                "78782EA4110C0C02281BF6026C18720C38D22800149C1181CC00010000260E000000000615F8012C05041102FF001058FD0D0A"));

        verifyNotNull(decoder, binary(
                "78787aa2110c0e06372c813601040000591200000000009d7c01040000591200000000009d7c01040000591200000000009d7c01040000591200000000009d7c01040000591200000000009d7c01040000591200000000009d7c01040000591200000000009d7c0104ff02001801eb4039d10000000000000004fabeb50d0a"));

        verifyNotNull(decoder, binary(
                "78782727110c0b0e170f850450059107f461ae001c7e0a81360104cb8a00bef32806030c02ff000316b10d0a"));

        verifyNotNull(decoder, binary(
                "78782322110c070f1b0270000000000000000000040081360104cb8a00bef3000000000523030d0a"));

        verifyNotNull(decoder, binary(
                "787880a2110b161010140136040000591200000000009d7c01020000591200000000009d7c01020000591200000000009d7c01020000591200000000009d7c01020000591200000000009d7c01020000591200000000009d7c01020000591200000000009d7c0102ff033c1e04ddc28aa6001801eb4039c90000000000000014db84730d0a"));

        verifyNotNull(decoder, binary(
                "78782ba701cc000000919100000000090617032b3836313832323038343735363200000000000000000100049fb60d0a"));

        verifyNotNull(decoder, binary(
                "787819a501cc0000009191000000000906170304050400010005f44d0d0a"));

        verifyNotNull(decoder, binary(
                "78782ca3110b10081336f000000000000000000004003901cc0000009191000000000906170304050400010003e0940d0a"));

        verifyNotNull(decoder, binary(
                "7878A3A2110B0603201501CC010000254E000000000615F804000000254E000000000615F804000000254E000000000615F804000000254E000000000615F804000000254E000000000615F804000000254E000000000615F804000000254E000000000615F80400FF08F483CD4DF4C0D750BD5F8BC5CECFB0D59DAFB459CDA8574E8424C6CC50BD5F6C7E1CC9B0D59D8AA718C90087363040E0C83496727B4DE4C7002919670D0A"));

        verifyNotNull(decoder, binary(
                "78786CA1110B0413093801CC01000025FC000000000618C10201000025FC000000000618C10201000025FC000000000618C10201000025FC000000000618C10201000025FC000000000618C10201000025FC000000000618C10201000025FC000000000618C10201FF0002000541D70D0A"));

        verifyPosition(decoder, binary(
                "787822220F0C1D023305C9027AC8180C46586000140001CC00287D001F71000001000820860D0A"));

        verifyAttributes(decoder, binary(
                "797900262100000000020043006f006d006d0061006e00640020006500720072006f0072002100236e850d0a"));

        verifyNotNull(decoder, binary(
                "787803691604130318491475905BD30E25001E10BBF7635D14759006E626560501CC0028660F213228660F1F2828660EA81E286610731428660F20140D0A"));

        verifyNotNull(decoder, binary(
                "787800691710231108500200cc080c4e2fa5640c4e2fa66e0d0a"));

        verifyNotNull(decoder, binary(
                "787800171710231108290200cc080c4e2fa5640c4e2fa5640d0a"));

        verifyNotNull(decoder, binary(
                "787800691710231109200400cc080c4e2fa55a0c4ec0025a0c4e2fa6640c583918640d0a"));

        verifyNotNull(decoder, binary(
                "787800691710231111210700cc080c4e2fa55a0c4ec0025a0c4e39295a0c583918640c4e2fa6640c4e2fa4640c4ec854640d0a"));

        verifyNotNull(decoder, binary(
                "787800171710231112510600cc080c4e2fa55a0c4e2fa55a0c4e2fa55a0c4e2fa55a0c4e2fa55a0c4e2fa55a0d0a"));

        verifyPosition(decoder, binary(
                "7878121011091c0b1b2999058508040097a89e0034520d0a"));

        verifyNotNull(decoder, binary(
                "78780869170928113413ac9e17b30808514494fcf6e148596cb0ce2c67bd4a6eb0ce2c67bd4b0018e7d4333e55ec086be7f2df5fe48d8c94fc6657e48d8cb8f378510600cc0400d37a3d4600d37a3c5000d37a3b6400d376716400d305ac6400d393506e0d0a"));

        verifyNotNull(decoder, binary(
                "787808171709281135331491827b75594dc8d719a9708452cad719a9708550cad719a97086521491827b75574cac9e17b308085dc8d71939633947cad71939633a480700cc0400d37a3d5a00d37a3d5a00d37a3d5a00d37a3d5a00d37a3d5a00d37a3d5a00d37a3d5a0d0a"));

        verifyNotNull(decoder, binary(
                "78783b281108111002050136041bcf0000bf09000000000000000000000000000000000000000000000000000000000000000000000000ff00020007d3280d0a"));

        verifyNotNull(decoder, binary(
                "7878412c11030b011c1f013604cb8a00b17754cb8a00bef357cb8a00b73f5fcb8900b0e25fcb8900b6655fcb8a00b74960cb8a00b178620701001801eb40393800bbbde10d0a"));

        verifyNotNull(decoder, binary(
                "7878412c11030b012629013604cb8a00b17757cb8a00b73f5bcb8a00b7495ecb8900b0e25fcb8a00b1b9620000000000ff0000000000ffff01001801eb40393e00c0e6340d0a"));

        verifyPosition(decoder, binary(
                "787822221106160a1016c60278019407c7783800040001940504700046fc01030100065f570d0a"));

        verifyAttributes(decoder, binary(
                "797900143311070609020b00000000a0030046000109e4610d0a"));

        verifyAttributes(decoder, binary(
                "7979003e32110707083819000901fe0a060f006a1e3f24000000000000000000000000000000000000000000000000000000000000000000000000000000012116ba0d0a"));

        verifyAttributes(decoder, binary(
                "7979007632110706090217000901fe0a060f006a1c2024060f0053a429060f006a1d21060f0053a720060f006f151d0000000000000000000000003844d9e7f7e1773d60e327a9e442405cf28628b9c640a42bb0fc0d0244d855a38c220a4c802aa8da7dab50b0e235ef32dd5348ee0ce77a52540000010a205a0d0a"));

        verifyAttributes(decoder, binary(
                "7979006f210000000001426174746572793a352e3536562c4e4f524d414c3b20475052532a4c696e6b2055702047534d205369676e616c204c6576656c3a5374726f6e673b204750533a4f46463b2020204c4f434b3a4f46463b204254204d41433a4234413832383034343436323b007260880d0a"));

        verifyPosition(decoder, binary(
                "7979004a321106170c1b180cc900a875580b7ab4f00010350901fe0a007c0009112424007c000912240081004efe2100c500100f1200000000000000000000000000000000000000000000bc7c900d0a"));

        verifyNotNull(decoder, binary(
                "79790045321106170c1b13000901fe0a007c0009112424007c000912230081004efe1e00c500100f120000000000000000000000000000000000000710bef565574e37000000b26f140d0a"));

        verifyNull(decoder, binary(
                "787811010863586038760942a0010000010aa4000d0a"));

        verifyNull(decoder, binary(
                "78781f3511061a0b24330503107d06084889cb01000100000cfa20e3acd301333fcb0d0a"));

        verifyPosition(decoder, binary(
                "78783c340000000011061809130c0903107d2408488a5800144c00000000000001940b00b1000047ff000000000500018f42210000000100050003010b69450d0a"));

        verifyPosition(decoder, binary(
                "78783c34000000001106190336070903107d51084889b900152e0000000043b101940b00b10000480100000000050001a3422100000001000300011bdc7b5f0d0a"));

        verifyAttributes(decoder, binary(
                "78780a13c40604000201298f5b0d0a"));

        verifyNotNull(decoder, binary(
                "78781f12110616091835d0024bb93007d3fb783dd4c501940500f2006c8504a6e0370d0a"));

        verifyPosition(decoder, binary(
                "787822221106160a1016c60278019407c7783800040001940504700046fc01030100065f570d0a"));

        verifyNotNull(decoder, binary(
                "7878661500000000004459443d537563636573732100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010009e82b0d0a"));

        verifyAttributes(decoder, binary(
                "7979000894000000000338ba0d0a"));

        verifyAttributes(decoder, binary(
                "79790020940a03516080825457290502194200448892980691312079088572f50004d4350d0a"));

        verifyPosition(decoder, binary(
                "7979007121000000000143757272656e7420706f736974696f6e214c61743a4e35342e3733393333322c4c6f6e3a4532352e3237333237302c436f757273653a3132362e35332c53706565643a302e303030302c4461746554696d653a323031372d30352d3236202031303a32373a3437000bbee30d0a"));

        verifyAttributes(decoder, binary(
                "7979003F940D110315102A202141494F494C2C30322C3030382E3239302C3032392E3630302C3531394A2C303430302C3030382E3433302C302C30302C4142001678EA0D0A"));

        verifyAttributes(decoder, binary(
                "79790039940d2141494f494c2c30322c3030322e3732302c3032392e3530302c3532344a2c303130302c3030332e3430302c302c30302c393309ad72000d0a"));

        verifyNull(decoder, binary(
                "79790005840016BB1A0D0A"));

        verifyAttributes(decoder, binary(
                "797900089400000002e852d70d0a"));

        verifyAttributes(decoder, binary(
                "7979000794050000c9b63e0d0a"));

        verifyNotNull(decoder, binary(
                "78783b18100c0f1201010195271784005ab63617840002fa47178400ff8f4817840019f3491784005ab54b178400ff8e4c17840019f24cff0002012287c80d0a"));

        verifyPosition(decoder, binary(
                "7878251610051b0f1c34c5022515d504b5dcd20738080902d4022bdf009cba5006640201006759680d0a"));

        verifyNotNull(decoder, binary(
                "787866150000000000416c726561647920696e20746865207374617465206f66206675656c20737570706c7920746f20726573756d652c74686520636f6d6d616e64206973206e6f742072756e6e696e672100000000000000000000000000000000000001001981e50d0a"));

        verifyAttributes(decoder, binary(
                "78782d152500000000437574206f666620746865206675656c20737570706c793a2053756363657373210002013b898a0d0a"));

        verifyAttributes(decoder, binary(
                "787829152100000000526573746f7265206675656c20737570706c793a2053756363657373210002014077ce0d0a"));

        verifyNull(decoder, binary(
                "78780D01012345678901234500018CDD0D0A"));

        verifyNull(decoder, binary(
                "78780d0103534190360660610003c3df0d0a"));

        verifyAttributes(decoder, binary(
                "78780a13440604000201baaf540d0a"));

        verifyAttributes(decoder, binary(
                "787825160F0C1D0A2B21C8027AC8040C46581000146F0901CC00287D001F714804040301001C84CF0D0A"));

        verifyPosition(decoder, binary(
                "78781f120f0a140e150bc505e51e780293a9e800540000f601006e0055da00035f240d0a"),
                position("2015-10-20 14:21:11.000", true, 54.94535, 24.01762));

        verifyPosition(decoder, binary(
                "787823120f081b121d37cb01c8e2cc08afd3c020d50201940701d600a1190041ee100576d1470d0a"));

        verifyPosition(decoder, binary(
                "78781F120B081D112E10CC027AC7EB0C46584900148F01CC00287D001FB8000380810D0A"));

        verifyPosition(decoder, binary(
                "787819100B031A0B1B31CC027AC7FD0C4657BF0115210001001CC6070D0A"));

        verifyPosition(decoder, binary(
                "787821120C010C0F151FCF027AC8840C4657EC00140001CC00287D001F720001000F53A00D0A"));

        verifyPosition(decoder, binary(
                "787825160B051B093523CF027AC8360C4657B30014000901CC00266A001E1740050400020008D7B10D0A"));

        verifyPosition(decoder, binary(
                "787819100e010903230ec803ae32a60653cded00180000020072feb70d0a"));

        verifyPosition(decoder, binary(
                "7878471e0e03110b0511c501c664fd074db73f0218a602e003433a002fed40433a0056e14e433a0056104e433a0056fd53433a002eed55433a007e4b57433a002ee25aff00020120f6720d0a"));

        verifyNull(decoder, binary(
                "7979005bfd0358899050927725004c0020bf984358df603b2ea3a339e54335013a5b56455253494f4e5d47543036445f32305f3630444d325f423235455f5631355f574d5b4255494c445d323031332f31322f32382031353a3234002a3b240d0a7979005bfd0358899050927725004c0020bf984358df603b2ea3a339e54335013a5b56455253494f4e5d47543036445f32305f3630444d325f423235455f5631355f574d5b4255494c445d323031332f31322f32382031353a3234002d4f9b0d0a7979005bfd0358899050927725004c0020bf984358df603b2ea3a339e54335013a5b56455253494f4e5d47543036445f32305f3630444d325f423235455f5631355f574d5b4255494c445d323031332f31322f32382031353a3234003084ff0d0a"));

        verifyNull(decoder, binary(
                "78788b818300000000534545464e2626004f04220045042626262b37393035343031353534362626262626260410041b0415041a04210415041926262b373930363433333031313526260410043d044f26262b373936303437383430363426260412043e0432043026262b373932383834373738383126262626262626262626262626262626232300020022155d0d0a"));

        verifyPosition(decoder, binary(
                "787822220e0914160f07c9021a362805090a7800d8b802d402c30e00a98a0105010213f4bb0d0a"));

        verifyNull(decoder, binary(
                "787811010864717003664467100f190a0002c6d20d0a"));

        verifyNull(decoder, binary(
                "787811010123456789012345100B3201000171930D0A"));

        verifyNull(decoder, binary(
                "78780d1f000000000000000200b196a20d0a"));

        verifyPosition(decoder, binary(
                "78781f12110819110216d402f250340828924055d4c801944600d300c09501429c830d0a"));

        verifyPosition(decoder, binary(
                "78782516110819110208d402f264dc08289a4058d4c70901944600d300c0954606040600014057e90d0a"));

        verifyNull(decoder, binary(
                "78780d010359339075005244340d0a"));

        verifyNotNull(decoder, binary(
                "787800691709261259400700cc0400d376714600d37a3d5000d37a3c5000d393505a00d3765d5a00d376735a00d32e6b640d0a"));

        verifyNull(decoder, binary(
                "787801080d0a"));

        verifyNull(decoder, binary(
                "78782E2A0F0C1D071139CA027AC8000C4658000014D83132353230313335333231373730373900000000000001002A6ECE0D0A"));

        verifyNull(decoder, binary(
                "7878058A000688290D0A"));

        verifyAttribute(decoder, binary(
                "78780c95130a0209321c90000112800d0a"),
                Position.KEY_ALARM, Position.ALARM_ACCELERATION);

        verifyAttribute(decoder, binary(
                "78780c95130a0209321c91000112800d0a"),
                Position.KEY_ALARM, Position.ALARM_BRAKING);

        verifyAttribute(decoder, binary(
                "78788b95140a060e2208055D4A800209D9C014c59100004556454e545f3335333337363131303032333139365f30303030303030305f323032305f31305f30365f31365f33345f30385f33322e6d70342c4556454e545f3335333337363131303032333139365f30303030303030305f323032305f31305f30365f31365f33345f30385f33312e6d70340000da360d0a"),
                Position.KEY_ALARM, Position.ALARM_BRAKING);

        verifyAttribute(decoder, binary(
                "78788b95140a070c1434055D4A800209D9C014280100004556454e545f3335333337363131303032333139365f30303030303030305f323032305f31305f30375f31345f32305f35325f32342e6d70342c4556454e545f3335333337363131303032333139365f30303030303030305f323032305f31305f30375f31345f32305f35325f32332e6d7034003f6b260d0a"),
                Position.KEY_ALARM, Position.ALARM_SOS);

        verifyAttribute(decoder, binary(
                "78785195140a020c2914055D4A800209D9C014009300004556454e545f3335333337363131303032333139365f30303030303030305f323032305f31305f30325f31345f34315f32305f30352e6d70340004e3a60d0a"),
                Position.KEY_ALARM, Position.ALARM_ACCIDENT);

    }

}
