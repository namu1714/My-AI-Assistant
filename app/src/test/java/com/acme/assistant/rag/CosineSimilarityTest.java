package com.acme.assistant.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CosineSimilarityTest {

    @Test
    void 동일한_벡터는_1을_반환한다() {
        float[] a = {1.0f, 2.0f, 3.0f};

        assertThat(CosineSimilarity.calculate(a, a))
                .isCloseTo(1.0, offset(0.0001));
    }
    @Test
    void 직교_벡터는_0을_반환한다() {
        float[] a = {1.0f, 0.0f};
        float[] b = {0.0f, 1.0f};

        assertThat(CosineSimilarity.calculate(a, b))
                .isCloseTo(0.0, offset(0.0001));
    }

    @Test
    void 반대_방향은_음수를_반환한다() {
        float[] a = {1.0f, 0.0f};
        float[] b = {-1.0f, 0.0f};

        assertThat(CosineSimilarity.calculate(a, b))
                .isCloseTo(-1.0, offset(0.0001));
    }
    @Test
    void 유사한_벡터는_높은_점수를_반환한다() {
        float[] a = {1.0f, 1.0f, 0.0f};
        float[] b = {1.0f, 0.9f, 0.1f};

        assertThat(CosineSimilarity.calculate(a, b))
                .isGreaterThan(0.9);
    }
    @Test
    void 영벡터는_0을_반환한다() {
        float[] zero = {0.0f, 0.0f, 0.0f};
        float[] any = {1.0f, 2.0f, 3.0f};

        assertThat(CosineSimilarity.calculate(zero, any))
                .isCloseTo(0.0, offset(0.0001));
    }
    @Test
    void 차원_불일치는_예외를_던진다() {
        float[] a = {1.0f, 2.0f};
        float[] b = {1.0f, 2.0f, 3.0f};

        assertThatThrownBy(() -> CosineSimilarity.calculate(a, b))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
