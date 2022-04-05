package org.springframework.data.r2dbc.repository.query;

import java.io.Serializable;
import java.util.List;

public class MementoPage<T> implements Serializable {
    private MementoPageRequest page;
    private List<T> content;

    public MementoPage() {}

    public MementoPage(MementoPageRequest page, List<T> content) {
        this.page = page;
        this.content = content;
    }
    public MementoPageRequest getPage() {
        return page;
    }

    public List<T> getContent() {
        return content;
    }

    public int getCount() {
        return content.size();
    }

    public boolean isLastPage() {
        return page.number == page.getTotalPages() - 1;
    }

    public boolean isFirstPage() {
        return page.number == 0;
    }

    public boolean isHasContent() {
        return !content.isEmpty();
    }

    public static class MementoPageRequest implements Serializable {
        private Integer number;
        private Integer size;
        private Long totalElements;
        private String sort;

        public MementoPageRequest() {}

        public MementoPageRequest(Dsl dsl, Long totalElements) {
            this.number = dsl.getPage() < 0 ? 0 : dsl.getPage();
            this.size = dsl.getSize() < 0 ? 20 : dsl.getSize();
            this.totalElements = totalElements;
            this.sort = dsl.getSort();
        }

        public int getOffset() {
            return number * size;
        }

        public int getTotalPages() {
            if (size == 0)
                return 1;
            else
                return Double.valueOf(Math.ceil(totalElements.doubleValue() / size.doubleValue())).intValue();
        }

        public Integer getNumber() {
            return number;
        }

        public Integer getSize() {
            return size;
        }

        public Long getTotalElements() {
            return totalElements;
        }

        public String getSort() {
            return sort;
        }
    }
}