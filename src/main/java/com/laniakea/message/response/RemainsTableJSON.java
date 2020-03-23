package com.laniakea.message.response;

import java.math.BigDecimal;
import java.util.Comparator;

public class RemainsTableJSON implements Comparable<RemainsTableJSON> {
    private Long        id;
    private String      name;
    private String      description;
    private String      article;
    private String      productgroup;
    private BigDecimal  quantity;
    private BigDecimal  min_quantity;
    private Integer     estimate_quantity;
    private Boolean     not_buy;
    private Boolean     not_sell;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEstimate_quantity() {
        return estimate_quantity;
    }

    public void setEstimate_quantity(Integer estimate_quantity) {
        this.estimate_quantity = estimate_quantity;
    }

    public Boolean getNot_sell() {
        return not_sell;
    }

    public void setNot_sell(Boolean not_sell) {
        this.not_sell = not_sell;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public Boolean getNot_buy() {
        return not_buy;
    }

    public void setNot_buy(Boolean not_buy) {
        this.not_buy = not_buy;
    }

    public String getProductgroup() {
        return productgroup;
    }

    public void setProductgroup(String productgroup) {
        this.productgroup = productgroup;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getMin_quantity() {
        return min_quantity;
    }

    public void setMin_quantity(BigDecimal min_quantity) {
        this.min_quantity = min_quantity;
    }

    @Override
    public int compareTo(RemainsTableJSON o) {
        return 0;
    }

    public static Comparator<RemainsTableJSON> COMPARE_BY_NAME_ASC= new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.name.compareTo(other.name);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_NAME_DESC= new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.name.compareTo(one.name);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_DESCRIPTION_ASC= new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.description.compareTo(other.description);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_DESCRIPTION_DESC= new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.description.compareTo(one.description);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_ARTICLE_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.article.compareTo(other.article);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_ARTICLE_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.article.compareTo(one.article);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_PRODUCTGROUP_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.productgroup.compareTo(other.productgroup);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_PRODUCTGROUP_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.productgroup.compareTo(one.productgroup);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_QUANTITY_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.quantity.compareTo(other.quantity);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_QUANTITY_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.quantity.compareTo(one.quantity);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_MINQUANTITY_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.min_quantity.compareTo(other.min_quantity);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_MINQUANTITY_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.min_quantity.compareTo(one.min_quantity);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_ESTIMATEQUANTITY_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.estimate_quantity-other.estimate_quantity;
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_ESTIMATEQUANTITY_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.estimate_quantity-one.estimate_quantity;
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_NOTBUY_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.not_buy.compareTo(other.not_buy);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_NOTBUY_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.not_buy.compareTo(one.not_buy);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_NOTSELL_ASC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return one.not_sell.compareTo(other.not_sell);
        }
    };
    public static Comparator<RemainsTableJSON> COMPARE_BY_NOTSELL_DESC = new Comparator<RemainsTableJSON>() {
        public int compare(RemainsTableJSON one, RemainsTableJSON other) {
            return other.not_sell.compareTo(one.not_sell);
        }
    };
}
