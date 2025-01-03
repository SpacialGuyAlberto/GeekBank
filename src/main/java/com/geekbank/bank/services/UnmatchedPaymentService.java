package com.geekbank.bank.services;

import com.geekbank.bank.models.UnmatchedPayment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UnmatchedPaymentService {

    /**
     * Crea un nuevo pago no coincidente.
     *
     * @param payment Objeto UnmatchedPayment con los detalles del pago.
     * @param image   Archivo de imagen opcional asociado al pago.
     * @return El pago no coincidente creado.
     * @throws IOException Si ocurre un error al procesar la imagen.
     */
    UnmatchedPayment createUnmatchedPayment(UnmatchedPayment payment, MultipartFile image) throws IOException;

    /**
     * Actualiza un pago no coincidente existente.
     *
     * @param id            ID del pago a actualizar.
     * @param paymentDetails Objeto UnmatchedPayment con los nuevos detalles del pago.
     * @param image         Archivo de imagen opcional para actualizar.
     * @return El pago no coincidente actualizado.
     * @throws IOException Si ocurre un error al procesar la imagen.
     */
    UnmatchedPayment updateUnmatchedPayment(Long id, UnmatchedPayment paymentDetails, MultipartFile image) throws IOException;

    /**
     * Obtiene un pago no coincidente por su ID.
     *
     * @param id ID del pago a obtener.
     * @return El pago no coincidente correspondiente.
     */
    UnmatchedPayment getUnmatchedPaymentById(Long id);

    /**
     * Obtiene todos los pagos no coincidentes.
     *
     * @return Lista de todos los pagos no coincidentes.
     */
    List<UnmatchedPayment> getAllUnmatchedPayments();

    /**
     * Elimina un pago no coincidente por su ID.
     *
     * @param id ID del pago a eliminar.
     */
    void deleteUnmatchedPayment(Long id);
}

