package com.laniakea.message.response;

import java.math.BigDecimal;
import java.util.Comparator;

public class PricesTableJSON  implements Comparable<PricesTableJSON> {
    private Long        id;
    private String      name;
    private String      article;
    private String      productgroup;
    private BigDecimal  price;
    private Boolean     not_buy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getProductgroup() {
        return productgroup;
    }

    public void setProductgroup(String productgroup) {
        this.productgroup = productgroup;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getNot_buy() {
        return not_buy;
    }

    public void setNot_buy(Boolean not_buy) {
        this.not_buy = not_buy;
    }

    @Override
    public int compareTo(PricesTableJSON o) {
        return 0;
    }

    public static Comparator<PricesTableJSON> COMPARE_BY_NAME_ASC= new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return one.name.compareTo(other.name);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_NAME_DESC= new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return other.name.compareTo(one.name);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_ARTICLE_ASC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return one.article.compareTo(other.article);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_ARTICLE_DESC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return other.article.compareTo(one.article);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_PRODUCTGROUP_ASC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return one.productgroup.compareTo(other.productgroup);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_PRODUCTGROUP_DESC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return other.productgroup.compareTo(one.productgroup);
        }
    };
        public static Comparator<PricesTableJSON> COMPARE_BY_PRICE_ASC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return one.price.compareTo(other.price);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_PRICE_DESC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return other.price.compareTo(one.price);
        }
    };

    public static Comparator<PricesTableJSON> COMPARE_BY_NOTBUY_ASC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return one.not_buy.compareTo(other.not_buy);
        }
    };
    public static Comparator<PricesTableJSON> COMPARE_BY_NOTBUY_DESC = new Comparator<PricesTableJSON>() {
        public int compare(PricesTableJSON one, PricesTableJSON other) {
            return other.not_buy.compareTo(one.not_buy);
        }
    };
}
