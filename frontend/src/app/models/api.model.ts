export type StatusTarefa='PENDENTE'|'EM_ANDAMENTO'|'CONCLUIDA';
export interface Tarefa{id:number;titulo:string;descricao:string|null;dataVencimento:string;status:StatusTarefa} export interface TarefaRequest{titulo:string;descricao:string;dataVencimento:string}
export interface Orcamento extends Tarefa{} export interface OrcamentoRequest extends TarefaRequest{}
export interface Produto{id:number;codigo:number;nome:string;preco:number;dataValidade:string|null} export interface ProdutoRequest{codigo:number;nome:string;preco:number;dataValidade:string|null}
export interface Usuario{id:number;nome:string;email:string} export interface UsuarioRequest{nome:string;email:string;senha:string}
export interface ApiError{mensagem?:string;campos?:Record<string,string>} export type ApiEntity=Tarefa|Orcamento|Produto|Usuario; export type ApiPayload=TarefaRequest|OrcamentoRequest|ProdutoRequest|UsuarioRequest;
