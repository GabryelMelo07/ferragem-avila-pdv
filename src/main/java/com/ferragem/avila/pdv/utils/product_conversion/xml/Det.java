package com.ferragem.avila.pdv.utils.product_conversion.xml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Setter;

@XmlRootElement(name = "det")
@Setter
public class Det {
    private ProductFromXML prod;

    @XmlElement(name = "prod")
    public ProductFromXML getProd() {
        return prod;
    }

}
