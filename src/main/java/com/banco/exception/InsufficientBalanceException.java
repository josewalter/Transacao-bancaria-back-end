package com.banco.exception;

public class InsufficientBalanceException extends TransferException{

    public InsufficientBalanceException(String message) {
        super(message); }
}
