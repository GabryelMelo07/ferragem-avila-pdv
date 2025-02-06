package com.ferragem.avila.pdv.utils.product_conversion.xml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "NFe")
public class Nfe {
    private InfNfe infNfe;

    @XmlElement(name = "infNFe")
    public InfNfe getInfNfe() {
        return infNfe;
    }

    public void setInfNfe(InfNfe infNfe) {
        this.infNfe = infNfe;
    }
}
