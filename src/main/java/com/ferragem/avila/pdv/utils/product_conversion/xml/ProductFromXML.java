package com.ferragem.avila.pdv.utils.product_conversion.xml;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Setter;

@XmlRootElement(name = "prod")
@Setter
public class ProductFromXML {
    private String xProd;
    private String uCom;
    private Float qCom;
    private BigDecimal vUnCom;
    private String cEAN;

    @XmlElement(name = "xProd")
    public String getXProd() {
        return xProd;
    }

    @XmlElement(name = "uCom")
    public String getUCom() {
        return uCom;
    }

    @XmlElement(name = "qCom")
    public Float getQCom() {
        return qCom;
    }

    @XmlElement(name = "vUnCom")
    public BigDecimal getVUnCom() {
        return vUnCom;
    }

    @XmlElement(name = "cEAN")
    public String getCEAN() {
        return cEAN;
    }

}
