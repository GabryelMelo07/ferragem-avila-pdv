package com.ferragem.avila.pdv.utils.product_conversion.xml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "nfeProc")
public class NfeProc {
    private Nfe nfe;

    @XmlElement(name = "NFe")
    public Nfe getNfe() {
        return nfe;
    }

    public void setNfe(Nfe nfe) {
        this.nfe = nfe;
    }
}
