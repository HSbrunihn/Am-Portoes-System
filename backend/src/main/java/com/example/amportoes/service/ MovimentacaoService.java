import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               ProdutoRepository produtoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(MovimentacaoDTO dto) {
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Regra de negócio
        if (dto.getTipo() == TipoMovimentacao.SAIDA && produto.getQuantidade() < dto.getQuantidade()) {
            throw new RuntimeException("Estoque insuficiente para saída");
        }

        if (dto.getTipo() == TipoMovimentacao.ENTRADA) {
            produto.setQuantidade(produto.getQuantidade() + dto.getQuantidade());
        } else {
            produto.setQuantidade(produto.getQuantidade() - dto.getQuantidade());
        }

        produtoRepository.save(produto);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setQuantidade(dto.getQuantidade());
        movimentacao.setTipo(dto.getTipo());
        movimentacao.setDataMovimentacao(dto.getDataMovimentacao() != null ? dto.getDataMovimentacao() : LocalDateTime.now());
        movimentacao.setObservacao(dto.getObservacao());

        return movimentacaoRepository.save(movimentacao);
    }
}