package com.kalek.crud.controller;



import com.kalek.crud.dto.Mensaje;
import com.kalek.crud.dto.ProductoDTO;
import com.kalek.crud.models.Producto;
import com.kalek.crud.service.ProductoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import javax.persistence.Convert;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/producto")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/listar")
    public List<ProductoDTO> listaDeProductos(){

        return productoService.listarProductos().stream().map(producto->modelMapper.map(producto,ProductoDTO.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<Producto> detalleProducto(@PathVariable Long id){
        if(!productoService.existeElId(id)){
            return new ResponseEntity(new Mensaje("No existe el id"),HttpStatus.NOT_FOUND);
        }
        Optional<Producto> producto=productoService.buscarPorId(id);
       return new ResponseEntity(producto,HttpStatus.OK);
    }

    @GetMapping("lista/{nombre}")
    public ResponseEntity<Producto> obtenerNombre(@PathVariable String nombre){
        if(!productoService.existeNombre(nombre)){
            return new ResponseEntity(new Mensaje("No existe el nombre"),HttpStatus.NOT_FOUND);
        }
        Producto producto =productoService.buscarPorNombre(nombre).get();
        return new ResponseEntity(producto,HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nuevo")
    public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO productoDTO){

        if(productoDTO.getPrecio() < 0){
            return new ResponseEntity(new Mensaje("El producto debe sr mayor que 0"),HttpStatus.BAD_REQUEST);
        }
        if(productoService.existeNombre(productoDTO.getNombre())){
            return new ResponseEntity(new Mensaje("Ya existe el producto con ese nombre"),HttpStatus.BAD_REQUEST);
        }
        //Dto a entity
        Producto productoReq = modelMapper.map(productoDTO, Producto.class);
        Producto producto=productoService.guardar(productoReq);
        //enttity a deto
        ProductoDTO productoRes=modelMapper.map(producto,ProductoDTO.class);
        return new ResponseEntity(productoRes,HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarProducto(@PathVariable Long id, @RequestBody ProductoDTO productoDTO){
        if(!productoService.existeElId(id)){
            return new ResponseEntity(new Mensaje("No existe el id"),HttpStatus.NOT_FOUND);
        }
        if(productoDTO.getPrecio()==null || productoDTO.getPrecio()<0){
            return new ResponseEntity(new Mensaje("El producto debe sr mayor que 0"),HttpStatus.BAD_REQUEST);
        }
        if(productoService.existeNombre(productoDTO.getNombre()) && productoService.buscarPorNombre(productoDTO.getNombre())
                .get().getId() != id){
            return new ResponseEntity(new Mensaje("Ya existe el producto con ese nombre"),HttpStatus.BAD_REQUEST);
        }
        Producto producto=productoService.buscarPorId(id).get();
        producto.setNombre(productoDTO.getNombre());
        producto.setPrecio(productoDTO.getPrecio());
        productoService.guardar(producto);
        return new ResponseEntity(new Mensaje("producto actualizado"),HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        if(!productoService.existeElId(id)){
            return new ResponseEntity(new Mensaje("No existe el id"),HttpStatus.NOT_FOUND);
        }
        productoService.eliminar(id);
        return new ResponseEntity(new Mensaje("producto eliminado"),HttpStatus.OK);
    }
}
