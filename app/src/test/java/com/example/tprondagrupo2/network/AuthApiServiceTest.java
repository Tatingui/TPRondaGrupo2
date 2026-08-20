package com.example.tprondagrupo2.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.LoginRequest;
import com.example.tprondagrupo2.model.OtpRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.model.RegisterRequest;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.http.POST;

public class AuthApiServiceTest {

    @Test
    public void testLoginEstaDeclaradoYDevuelveCallDeAuthResponse() {
        // Valida la firma del endpoint de login
        assertFirmaValida("login", LoginRequest.class);
    }

    @Test
    public void testRegisterEstaDeclaradoYDevuelveCallDeAuthResponse() {
        // Valida la firma del endpoint de registro
        assertFirmaValida("register", RegisterRequest.class);
    }

    @Test
    public void testSendOtpEstaDeclaradoYDevuelveCallDeAuthResponse() {
        // Valida la firma del endpoint de envio de OTP
        assertFirmaValida("sendOtp", OtpSendRequest.class);
    }

    @Test
    public void testVerifyOtpEstaDeclaradoYDevuelveCallDeAuthResponse() {
        // Valida la firma del endpoint de verificacion de OTP
        assertFirmaValida("verifyOtp", OtpRequest.class);
    }

    @Test
    public void testResendOtpEstaDeclaradoYDevuelveCallDeAuthResponse() {
        // Valida la firma del endpoint de reenvio de OTP
        assertFirmaValida("resendOtp", OtpSendRequest.class);
    }

    @Test
    public void testLoginTieneLaRutaPostCorrecta() {
        // Valida que login apunta a auth/login
        assertRutaPost("login", LoginRequest.class, "auth/login");
    }

    @Test
    public void testRegisterTieneLaRutaPostCorrecta() {
        // Valida que register apunta a auth/register
        assertRutaPost("register", RegisterRequest.class, "auth/register");
    }

    @Test
    public void testSendOtpTieneLaRutaPostCorrecta() {
        // Valida que sendOtp apunta a auth/otp/send
        assertRutaPost("sendOtp", OtpSendRequest.class, "auth/otp/send");
    }

    @Test
    public void testVerifyOtpTieneLaRutaPostCorrecta() {
        // Valida que verifyOtp apunta a auth/otp/verify
        assertRutaPost("verifyOtp", OtpRequest.class, "auth/otp/verify");
    }

    @Test
    public void testResendOtpTieneLaRutaPostCorrecta() {
        // Valida que resendOtp apunta a auth/otp/resend
        assertRutaPost("resendOtp", OtpSendRequest.class, "auth/otp/resend");
    }

    @Test
    public void testLaInterfazTieneExactamenteCincoMetodos() {
        // Valida que no se agregaron ni borraron endpoints sin actualizar los tests
        assertEquals(5, AuthApiService.class.getDeclaredMethods().length);
    }

    /**
     * Busca el metodo por nombre y tipo de parametro, y verifica que devuelva Call<AuthResponse>.
     */
    private void assertFirmaValida(String nombreMetodo, Class<?> tipoParametro) {
        Method metodo = buscarMetodo(nombreMetodo, tipoParametro);

        assertEquals(Call.class, metodo.getReturnType());

        Type tipoGenerico = metodo.getGenericReturnType();
        assertTrue("El retorno de " + nombreMetodo + " deberia ser parametrizado",
                tipoGenerico instanceof ParameterizedType);

        Type[] argumentos = ((ParameterizedType) tipoGenerico).getActualTypeArguments();
        assertEquals(1, argumentos.length);
        assertEquals(AuthResponse.class, argumentos[0]);
    }

    /**
     * Verifica que el metodo tenga la anotacion @POST con la ruta esperada.
     */
    private void assertRutaPost(String nombreMetodo, Class<?> tipoParametro, String rutaEsperada) {
        Method metodo = buscarMetodo(nombreMetodo, tipoParametro);

        POST anotacion = metodo.getAnnotation(POST.class);
        assertNotNull("Falta @POST en " + nombreMetodo, anotacion);
        assertEquals(rutaEsperada, anotacion.value());
    }

    private Method buscarMetodo(String nombreMetodo, Class<?> tipoParametro) {
        try {
            Method metodo = AuthApiService.class.getMethod(nombreMetodo, tipoParametro);
            assertNotNull(metodo);
            return metodo;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("No existe el metodo " + nombreMetodo
                    + "(" + tipoParametro.getSimpleName() + ") en AuthApiService", e);
        }
    }
}
