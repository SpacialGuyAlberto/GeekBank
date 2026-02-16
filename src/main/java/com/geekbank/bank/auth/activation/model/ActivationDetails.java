package com.geekbank.bank.auth.activation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activation_details")
@Getter
@Setter
public class ActivationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kinguin_id", nullable = false, unique = true)
    private Long kinguinId;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "text_details", columnDefinition = "TEXT")
    private String textDetails;

    public ActivationDetails() {
    }

    public ActivationDetails(Long id, Long kinguinId, String videoUrl, String textDetails) {
        this.id = id;
        this.kinguinId = kinguinId;
        this.videoUrl = videoUrl;
        this.textDetails = textDetails;
    }

    // Métodos equals y hashCode para garantizar la unicidad y permitir comparaciones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivationDetails that = (ActivationDetails) o;

        if (!id.equals(that.id)) return false;
        return kinguinId.equals(that.kinguinId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + kinguinId.hashCode();
        return result;
    }

    // Método toString para facilitar la depuración
    @Override
    public String toString() {
        return "ActivationDetails{" +
                "id=" + id +
                ", kinguinId=" + kinguinId +
                ", videoUrl='" + videoUrl + '\'' +
                ", textDetails='" + textDetails + '\'' +
                '}';
    }
}
