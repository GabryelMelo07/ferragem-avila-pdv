package com.ferragem.avila.pdv.utils.product_conversion.xml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "infNFe")
public class InfNfe {
    private List<Det> detList;

    @XmlElement(name = "det")
    public List<Det> getDetList() {
        return detList;
    }

    public void setDetList(List<Det> detList) {
        this.detList = detList;
    }
}
