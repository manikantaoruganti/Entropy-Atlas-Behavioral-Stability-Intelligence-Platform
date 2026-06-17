import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  const pageNumbers = [];
  const maxPageButtons = 5;

  let startPage = Math.max(1, currentPage - Math.floor(maxPageButtons / 2));
  let endPage = Math.min(totalPages, startPage + maxPageButtons - 1);

  if (endPage - startPage + 1 < maxPageButtons) {
    startPage = Math.max(1, endPage - maxPageButtons + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button
        className="pagination-btn"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
      >
        <ChevronLeft className="w-4 h-4" />
      </button>

      {startPage > 1 && (
        <>
          <button className="pagination-btn" onClick={() => onPageChange(1)}>1</button>
          {startPage > 2 && <span className="text-text-muted px-2">...</span>}
        </>
      )}

      {pageNumbers.map((page) => (
        <button
          key={page}
          className={`pagination-btn ${page === currentPage ? 'active' : ''}`}
          onClick={() => onPageChange(page)}
        >
          {page}
        </button>
      ))}

      {endPage < totalPages && (
        <>
          {endPage < totalPages - 1 && <span className="text-text-muted px-2">...</span>}
          <button className="pagination-btn" onClick={() => onPageChange(totalPages)}>
            {totalPages}
          </button>
        </>
      )}

      <button
        className="pagination-btn"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
      >
        <ChevronRight className="w-4 h-4" />
      </button>
    </div>
  );
};

export default Pagination;
