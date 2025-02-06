package com.ferragem.avila.pdv.utils.product_conversion.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;

public class XmlParserUtil {
    public static NfeProc parseXml(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(NfeProc.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (NfeProc) unmarshaller.unmarshal(new StringReader(xml));
    }
}
